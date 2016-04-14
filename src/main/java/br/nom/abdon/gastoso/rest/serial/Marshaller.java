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
import br.nom.abdon.gastoso.aggregate.Saldo;
import br.nom.abdon.gastoso.rest.model.FatoNormal;
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
     * @param gen
     * @param fatoNormal
     * @param tipo
     * @throws IOException 
     */
    public static void marshall(
            final JsonGenerator gen, 
            final FatoNormal fatoNormal,
            final TIPO tipo) throws IOException {
        
        gen.writeStartObject();
        Marshaller.fatoCore(gen, fatoNormal);

        final List<Lancamento> lancamentos = 
            fatoNormal.getLancamentos();
        
        switch(lancamentos.size()) {
            case 1:
                final Lancamento lancamento = lancamentos.get(0);
                final Conta conta = lancamento.getConta();
    
                Marshaller.writeContaOrFields(gen, conta, tipo);
                Marshaller.writeValorField(gen, lancamento.getValor());
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

                    if(tipo == TIPO.BARE){
                        gen.writeNumberField("origemId", origem.getId());
                        gen.writeNumberField("destinoId", destino.getId());
                    } else {                
                        Marshaller.writeContaField(gen, "origem", origem, tipo);
                        Marshaller.writeContaField(gen, "destino", destino, tipo);
                    }
                    Marshaller.writeValorField(gen, destino.getValor());
                    break;
                }

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

    private static void writeContaOrFields(
            final JsonGenerator gen, 
            final Conta conta,
            final TIPO tipo) throws IOException {
        
        if(tipo == TIPO.BARE){
            Marshaller.writeContaIdField(gen, conta);
        } else {
            Marshaller.writeContaField(gen, conta, tipo);
        }
    }

    /**
     * Escreve um lancamento como faz sentido no contexto de lancamentos de uma
     * conta (o extrato da conta). Vai conter as informacoes do Fato (mas nao seus
     * lancamentos) e o valor do lancamento. A conta fica implicita.
     * 
     * @param gen
     * @param lancamento
     * @throws IOException 
     */
    public static void marshall(
            final JsonGenerator gen,
            final Lancamento lancamento) throws IOException {

        gen.writeStartObject();
        Marshaller.fatoCore(gen, lancamento.getFato());
        Marshaller.writeValorField(gen, lancamento.getValor());
        gen.writeEndObject();
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
            final Lancamento lancamentoDoFato,
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
            final Lancamento lancamento,
            final TIPO tipo) throws IOException{
    
        gen.writeStartObject();
        writeFatoLancamentoFields(gen, lancamento, tipo);
        gen.writeEndObject();
    }

    private static void writeFatoLancamentoFields(
            final JsonGenerator gen, 
            final Lancamento lancamento, 
            final TIPO tipo) throws IOException {

        Marshaller.writeContaOrFields(gen, lancamento.getConta(),tipo);
        Marshaller.writeValorField(gen, lancamento.getValor());
    }

    private static void writeContaIdField(
            final JsonGenerator gen, 
            final Conta conta) throws IOException {
        gen.writeNumberField("contaId", conta.getId());
    }
    
    private static void writeValorField(
            final JsonGenerator gen,
            final long valor) throws IOException{
        writeValorField(gen, "valor", valor);
    }

    private static void writeValorField(
            final JsonGenerator gen,
            String fieldName,
            final long valor) throws IOException{
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