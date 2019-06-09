/*
 * Copyright (C) 2016 Bruno Abdon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.github.brunoabdon.gastoso.rest.serial;

import java.io.IOException;
import java.time.LocalDate;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.core.JsonGenerator;
import pl.touk.throwing.ThrowingConsumer;

import com.github.brunoabdon.gastoso.Conta;
import com.github.brunoabdon.gastoso.Fato;
import com.github.brunoabdon.gastoso.Lancamento;
import com.github.brunoabdon.gastoso.ext.FatoDetalhado;
import com.github.brunoabdon.gastoso.ext.Saldo;
import com.github.brunoabdon.gastoso.rest.MediaTypes;
import static com.github.brunoabdon.gastoso.rest.MediaTypes.APPLICATION_GASTOSO_FULL_TYPE;
import static com.github.brunoabdon.gastoso.rest.MediaTypes.APPLICATION_GASTOSO_PATCH_TYPE;
import static com.github.brunoabdon.gastoso.rest.MediaTypes.APPLICATION_GASTOSO_SIMPLES_TYPE;


/**
 *
 * @author Bruno Abdon
 */
class Marshaller {

    private final JsonGenerator gen;
    
    public Marshaller(final JsonGenerator gen, final MediaType mediaType)
            throws IOException{
        this.gen = gen;
    }

    /**
     * Escreve a conta com o id caso ele exista e com o nome se:
     * 
     * <ol>
     *    <li>O mediaType for NORMAL; ou</li>
     *    <li>O mediaType for FULL; ou
     *    <li>Não tiver id</li>
     * </ol>
     * 
     * @param conta
     * @throws IOException 
     */
    public void marshall(final Conta conta, final MediaType tipo) 
            throws IOException {

        gen.writeStartObject();
        
        if(tipo != APPLICATION_GASTOSO_PATCH_TYPE)
            this.writeIdField(conta.getId());
        
        if(tipo != APPLICATION_GASTOSO_SIMPLES_TYPE)
            gen.writeStringField(Serial.NOME, conta.getNome());
        
        gen.writeEndObject();

    }

    public void marshall(final Saldo saldo, final MediaType tipo)
            throws IOException{
        gen.writeStartObject();
        if(tipo == MediaTypes.APPLICATION_GASTOSO_FULL_TYPE){
            this.writeContaField(saldo.getConta());
            this.writeDiaField(saldo.getDia());
        }
        this.writeValorField(saldo.getValor());
        gen.writeEndObject();
    }

    
    public void marshall(
                final Lancamento lancamento, 
                @SuppressWarnings("unused") //sempre eh simple (no marshall) 
                final MediaType tipo) 
            throws IOException {
        
        final Fato fato = lancamento.getFato();
        
        gen.writeStartObject();

        gen.writeNumberField(Serial.FATO_ID, fato.getId());
        this.writeDiaField(fato.getDia());
        this.writeDescField(fato.getDescricao());
        this.writeContaIdField(lancamento.getConta());
        this.writeValorField(lancamento.getValor());
        
        gen.writeEndObject();
    }
    
    public void marshall(final Fato fato, final MediaType tipo)
            throws IOException {

        gen.writeStartObject();
        
        //id
        final Integer idFato = fato.getId();
        if(tipo != APPLICATION_GASTOSO_PATCH_TYPE){
            this.writeIdField(idFato);
        }
        
        //dia
        final LocalDate dia = fato.getDia();
        if(tipo != APPLICATION_GASTOSO_PATCH_TYPE || dia != null){
            this.writeDiaField(dia);
        }
        
        //desc
        final String desc = fato.getDescricao();
        if(tipo != APPLICATION_GASTOSO_PATCH_TYPE || desc != null){
            this.writeDescField(desc);
        }
        
        if(fato instanceof FatoDetalhado){
            final FatoDetalhado fatoDetalhado = (FatoDetalhado)fato;
        
            final List<Lancamento> lancamentos = fatoDetalhado.getLancamentos();

            switch(lancamentos.size()) {
                case 1:
                    final Lancamento lancamento = lancamentos.get(0);
                    final Conta conta = lancamento.getConta();

                    if(tipo == APPLICATION_GASTOSO_FULL_TYPE){
                        this.writeContaField(conta);
                    } else {
                        this.writeContaIdField(conta);
                    }
                    this.writeValorField(lancamento.getValor());
                    break;

                case 2:
                    final Lancamento l0 = lancamentos.get(0);
                    final Lancamento l1 = lancamentos.get(1);

                    final int valor0 = l0.getValor();
                    final int valor1 = l1.getValor();

                    if(valor0 == -valor1){

                        final Lancamento origem =
                            valor0 < valor1 ? l0 : l1;
                        final Lancamento destino = l0 == origem ? l1 : l0;

                        if(tipo == APPLICATION_GASTOSO_FULL_TYPE){
                            this.writeContaField(Serial.ORIGEM, origem);
                            this.writeContaField(Serial.DESTINO, destino);
                        } else {                
                            this.writeContaIdField(Serial.ORIGEM_ID, origem);
                            this.writeContaIdField(Serial.DESTINO_ID, destino);
                        }
                        this.writeValorField(destino.getValor());
                        break;
                    }

                default:
                    gen.writeArrayFieldStart(Serial.LANCAMENTOS);
                    foreach(
                        lancamentos, l -> {
                            gen.writeStartObject();
                            final Conta c = l.getConta();
                            if(tipo == APPLICATION_GASTOSO_FULL_TYPE){
                                this.writeContaField(c);
                            } else {
                                this.writeContaIdField(c);
                            }
                            this.writeValorField(l.getValor());
                            gen.writeEndObject();
                        }
                    );
                    gen.writeEndArray();
                    break;
            }
        }
        gen.writeEndObject();
    }
    
    private void writeIdField(final int id) throws IOException {
        gen.writeNumberField(Serial.ID, id);
    }

    private void writeContaIdField(final Conta conta) throws IOException {
        gen.writeNumberField(Serial.CONTA_ID, conta.getId());
    }

    private void writeDiaField(final LocalDate dia) throws IOException {
        gen.writeStringField(Serial.DIA, dia.format(ISO_LOCAL_DATE));
    }

    private void writeValorField(final long valor) throws IOException {
        gen.writeNumberField(Serial.VALOR, valor);
    }

    private void writeDescField(final String desc) throws IOException {
        gen.writeStringField(Serial.DESC, desc);
    }
    
    private void writeContaField(final String fieldName, final Conta conta) 
            throws IOException{
        gen.writeFieldName(fieldName);
        gen.writeStartObject();
        this.writeIdField(conta.getId());
        gen.writeStringField(Serial.NOME, conta.getNome());
        gen.writeEndObject();
    }
    
    private void writeContaField(final Conta conta) throws IOException {
        this.writeContaField(Serial.CONTA,conta);
    }

    private void writeContaField(
            final String fieldName,
            final Lancamento lancamentoDoFato)
                throws IOException {
        this.writeContaField(fieldName, lancamentoDoFato.getConta());
    }
    
    private void writeContaIdField(
            final String fieldName,
            final Lancamento lancamentoDoFato) throws IOException{
        gen.writeNumberField(fieldName, lancamentoDoFato.getConta().getId());
    }

    //utility function.. move to abd-utils someday...
    private <E, Ex extends Exception> void foreach(
        final Collection<E> colection, 
        final ThrowingConsumer<E,Ex> consumer) throws Ex{

        for(final E e : colection) {
            consumer.accept(e);
        }
    }
}