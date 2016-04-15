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
package br.nom.abdon.gastoso.rest.serial;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import java.util.Collection;

import javax.ws.rs.core.MediaType;

import com.fasterxml.jackson.core.JsonGenerator;
import pl.touk.throwing.ThrowingConsumer;

import br.nom.abdon.gastoso.Conta;
import br.nom.abdon.gastoso.Fato;
import br.nom.abdon.gastoso.Lancamento;
import br.nom.abdon.gastoso.aggregate.Saldo;
import br.nom.abdon.gastoso.rest.MediaTypes;
import static br.nom.abdon.gastoso.rest.MediaTypes.APPLICATION_GASTOSO_SIMPLES_TYPE;
import br.nom.abdon.gastoso.aggregate.FatoDetalhado;

/**
 *
 * @author Bruno Abdon
 */
public class Marshaller {

    private final JsonGenerator gen;
    private final MediaType tipo;

    public Marshaller(final JsonGenerator gen, final MediaType mediaType)
            throws IOException{
        this.gen = gen;
        this.tipo = MediaTypes.getCompatibleInstance(mediaType);
    }
    
    public void marshall(final Conta conta) throws IOException {
        gen.writeStartObject();
        this.contaCore(conta);
        if(tipo != APPLICATION_GASTOSO_SIMPLES_TYPE){
            gen.writeStringField(Serial.NOME, conta.getNome());
        }
        gen.writeEndObject();
    }

    public void marshall(final Saldo saldo) throws IOException{
        gen.writeStartObject();
        if(tipo != APPLICATION_GASTOSO_SIMPLES_TYPE){
            this.writeContaField(saldo.getConta());
            this.writeDiaField(saldo.getDia());
        }
        this.writeValorField(saldo.getValor());
        gen.writeEndObject();
    }

    /**
     * Escreve um fato com informacao minimizada sobre seus lancamentos.
     * 
     * Sempre vai conter as informacoes basicas do fato (id, dia, descricao).
     * 
     * Caso tenha apenas um lancamento, vai ter os atributos da 'conta' e do
     * 'valor' do lancamento diretamente.
     * 
     * Caso tenha dois lancamentos com valores com soma zero (transferência),
     * vai contar os atributos 'origem' e 'destino' para as contas com valor
     * positivo e negativo respectivamente, e o atributo 'valor' com o valor
     * positivo.
     * 
     * Caso tenha outro numero qualquer de lancamentos, vai ter o atributo
     * 'lancamentos' com um array de lancamentos onde cada um tem a conta e o
     * valor.
     * 
     * Sempre, contas serao exibidas de acordo como o TIPO passado como 
     * parâmetro.
     * 
     * 
     * @param fatoNormal
     * @throws IOException 
     */
    public void marshall(final FatoDetalhado fatoNormal) throws IOException {

        gen.writeStartObject();
        this.fatoCore(fatoNormal);
        
        final List<Lancamento> lancamentos = fatoNormal.getLancamentos();

        switch(lancamentos.size()) {
            case 1:
                final Lancamento lancamento = lancamentos.get(0);
                final Conta conta = lancamento.getConta();

                this.writeContaOrFields(conta);
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

                    if(tipo == APPLICATION_GASTOSO_SIMPLES_TYPE){
                        gen.writeNumberField(Serial.ORIGEM_ID, origem.getId());
                        gen.writeNumberField(Serial.DESTINO_ID, destino.getId());
                    } else {                
                        this.writeContaField(Serial.ORIGEM, origem);
                        this.writeContaField(Serial.DESTINO, destino);
                    }
                    this.writeValorField(destino.getValor());
                    break;
                }

            default:
                gen.writeArrayFieldStart(Serial.LANCAMENTOS);
                foreach(
                    lancamentos, l -> this.writeFatoLancamento(l)
                );
                gen.writeEndArray();
                break;
        }

        gen.writeEndObject();
    }

    /**
     * Escreve um lancamento como faz sentido no contexto de lancamentos de uma
     * conta (o extrato da conta). Vai conter as informacoes do Fato (mas nao 
     * seus lancamentos) e o valor do lancamento. A conta fica implicita.
     * 
     * @param lancamento
     * @throws IOException 
     */
    public void marshall(final Lancamento lancamento) throws IOException {
        gen.writeStartObject();
        this.fatoCore(lancamento.getFato());
        this.writeValorField(lancamento.getValor());
        gen.writeEndObject();
    }

    private void writeContaOrFields(final Conta conta) throws IOException {
        if(tipo == APPLICATION_GASTOSO_SIMPLES_TYPE){
            this.writeContaIdField(conta);
        } else {
            this.writeContaField(conta);
        }
    }

    private void writeContaField(final Conta conta) throws IOException {
        this.writeContaField(Serial.CONTA,conta);
    }

    private void writeContaField(
            final String fieldName,
            final Lancamento lancamentoDoFato) throws IOException{
        this.writeContaField(fieldName, lancamentoDoFato.getConta());
    }

    private void writeContaField(final String fieldName, final Conta conta) 
            throws IOException{
        gen.writeFieldName(fieldName);
        this.marshall(conta);
    }

    private void writeFatoLancamento(
            final Lancamento lancamento) throws IOException{
        gen.writeStartObject();
        this.writeFatoLancamentoFields(lancamento);
        gen.writeEndObject();
    }

    private void writeFatoLancamentoFields(
            final Lancamento lancamento) throws IOException {
        this.writeContaOrFields(lancamento.getConta());
        this.writeValorField(lancamento.getValor());
    }

    private void writeContaIdField(final Conta conta) throws IOException {
        gen.writeNumberField(Serial.CONTA_ID, conta.getId());
    }

    private void writeValorField(final long valor) throws IOException{
        this.writeValorField(Serial.VALOR, valor);
    }

    private void writeValorField(final String fieldName, final long valor) 
            throws IOException{
        gen.writeNumberField(fieldName, valor);
    }

    private void writeDiaField(final LocalDate dia) throws IOException{
        this.writeDiaField(Serial.DIA, dia);
    }

    private void writeDiaField(final String fieldName, final LocalDate dia)
            throws IOException{
        gen.writeStringField(fieldName, dia.format(ISO_LOCAL_DATE));
    }

    private void writeIdField(final Integer id)
            throws IOException {
        gen.writeNumberField(Serial.ID, id);
    }

    private void contaCore(final Conta conta) throws IOException {
        this.writeIdField(conta.getId());
    }

    private void fatoCore(final Fato fatoNormal) throws IOException {
        this.writeIdField(fatoNormal.getId());
        this.writeDiaField(fatoNormal.getDia());
        gen.writeStringField(Serial.DESC, fatoNormal.getDescricao());
    }

    //utility function.. move to abd-utils someday...
    private <E, Ex extends Exception> void foreach(
        final Collection<E> colection, 
        final ThrowingConsumer<E,Ex> consumer) throws Ex{

        for(E e : colection) {
            consumer.accept(e);
        }
    }
}