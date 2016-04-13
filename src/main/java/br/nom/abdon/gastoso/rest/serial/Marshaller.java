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

import com.fasterxml.jackson.core.JsonGenerator;
import pl.touk.throwing.ThrowingConsumer;

import br.nom.abdon.gastoso.Conta;
import br.nom.abdon.gastoso.Fato;
import br.nom.abdon.gastoso.Lancamento;
import br.nom.abdon.gastoso.rest.mdl.FatoNormal;
import br.nom.abdon.gastoso.rest.mdl.Saldo;
import br.nom.abdon.modelo.Entidade;

/**
 *
 * @author Bruno Abdon
 */
public class Marshaller {

    public enum TIPO {BARE,NORM,FULL};

    public static void marshall(
            final JsonGenerator gen, 
            final Conta conta, 
            final TIPO tipo) throws IOException {
        
        gen.writeStartObject();
        Marshaller.contaCore(gen, conta);
        if(tipo != TIPO.BARE){
            gen.writeStringField("nome", conta.getNome());
        }
        gen.writeEndObject();
    }

    public static void marshall(
            final JsonGenerator gen, 
            final Saldo saldo, 
            final TIPO tipo) throws IOException {
        
        gen.writeStartObject();
        if(tipo != TIPO.BARE){
            Marshaller.writeContaField(gen, saldo.getConta(), TIPO.NORM);
            Marshaller.writeDiaField(gen, saldo.getDia());
        }
        Marshaller.writeValorField(gen, saldo.getValor());
        gen.writeEndObject();
    }
    
    public static void marshallSaldos(
            final JsonGenerator gen, 
            final List<Saldo> saldos, 
            final TIPO tipo) throws IOException {
        
        gen.writeStartArray();
        Marshaller.foreach(saldos, s -> Marshaller.marshall(gen, s, tipo));
        gen.writeEndArray();
    }

    public static void marshall(
            final JsonGenerator gen, 
            final FatoNormal fatoNormal,
            final TIPO tipo) throws IOException {
        
        gen.writeStartObject();
        fatoCore(gen, fatoNormal);

        final List<FatoNormal.Lancamento> lancamentos = 
            fatoNormal.getLancamentos();

        switch(lancamentos.size()) {
            case 1:
                Marshaller.writeFatoLancamento(gen, lancamentos.get(0), tipo);
                break;

            case 2:

                final FatoNormal.Lancamento l0 = lancamentos.get(0);
                final FatoNormal.Lancamento l1 = lancamentos.get(1);

                final FatoNormal.Lancamento origem =
                    l0.getValor() < l1.getValor() ? l0 : l1;
                final FatoNormal.Lancamento destino = l0 == origem ? l1 : l0;

                Marshaller.writeContaField(gen, "origem", origem, tipo);
                Marshaller.writeContaField(gen, "destino", destino, tipo);
                Marshaller.writeValorField(gen, destino.getValor());
                break;

            default:
                gen.writeArrayFieldStart("lancamentos");
                foreach(
                    lancamentos,
                    l -> Marshaller.writeFatoLancamento(gen, l, tipo)
                );  gen.writeEndArray();
                break;
        }
        
        gen.writeEndObject();
    }
    
    public static void marshallExtrato(
            final JsonGenerator gen, 
            final Conta conta,
            final List<Lancamento> lancamentos) throws IOException {

        gen.writeStartArray();
        Marshaller.foreach(
            lancamentos,
            lancamento -> {
                gen.writeStartObject();
                Marshaller.fatoCore(gen, lancamento.getFato());
                Marshaller.writeValorField(gen, lancamento.getValor());
                gen.writeEndObject();
            });
                
        gen.writeEndArray();
    }
    
    private static void writeContaField(
            final JsonGenerator gen, 
            final Conta conta, 
            final TIPO tipo) throws IOException{

        Marshaller.writeContaField(gen,"conta",conta,tipo);
    }

    private static void writeContaField(
            final JsonGenerator gen, 
            final String fieldName,
            final FatoNormal.Lancamento lancamentoDoFato,
            final TIPO tipo) throws IOException{
        
        Marshaller
            .writeContaField(
                gen, 
                fieldName, 
                lancamentoDoFato.getConta(), 
                tipo);
    }
    
    private static void writeContaField(
            final JsonGenerator gen, 
            final String fieldName,
            final Conta conta, 
            final TIPO tipo) throws IOException{
        gen.writeFieldName(fieldName);
        Marshaller.marshall(gen, conta, tipo);
    }

    private static void writeFatoLancamento(
            final JsonGenerator gen,
            final FatoNormal.Lancamento lancamento,
            final TIPO tipo) throws IOException{
    
        gen.writeStartObject();
        Marshaller.writeContaField(gen, lancamento.getConta(), tipo);
        Marshaller.writeValorField(gen, lancamento.getValor());
        gen.writeEndObject();
    }
    
    private static void writeValorField(
            final JsonGenerator gen,
            final int valor) throws IOException{
        writeValorField(gen, "valor", valor);
    }

    private static void writeValorField(
            final JsonGenerator gen,
            String fieldName,
            final int valor) throws IOException{
        gen.writeNumberField(fieldName, valor);
    }

    private static void writeDiaField(
            final JsonGenerator gen,
            final LocalDate dia) throws IOException{
        writeDiaField(gen, "dia", dia);
    }

    private static void writeDiaField(
            final JsonGenerator gen,
            final String fieldName,
            final LocalDate dia) throws IOException{
        gen.writeStringField(fieldName, dia.format(ISO_LOCAL_DATE));
    }
    
    private static void writeIdField(
            final JsonGenerator gen, 
            final Entidade<Integer> entidade) throws IOException {
        gen.writeNumberField("id", entidade.getId());
    }
    
    private static void contaCore(
            final JsonGenerator gen, 
            final Conta conta) throws IOException {
        Marshaller.writeIdField(gen, conta);
    }
    
    private static void fatoCore(
            final JsonGenerator gen, 
            final Fato fatoNormal) throws IOException {
        Marshaller.writeIdField(gen, fatoNormal);
        Marshaller.writeDiaField(gen, fatoNormal.getDia());
        gen.writeStringField("desc", fatoNormal.getDescricao());
    }
 
    //utility function.. move to abd-utils someday...
    private static <E, Ex extends Exception> void foreach(
        final Collection<E> colection, 
        final ThrowingConsumer<E,Ex> consumer) throws Ex{
        
        for(E e : colection) {
            consumer.accept(e);
        }
    }

}