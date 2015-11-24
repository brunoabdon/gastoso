/*
 * Copyright (C) 2015 Bruno Abdon
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
package br.nom.abdon.gastoso.rest.model;

import br.nom.abdon.gastoso.Conta;
import br.nom.abdon.gastoso.rest.serial.FatosJsonSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 *
 * @author Bruno Abdon
 */
@JsonSerialize(using = FatosJsonSerializer.class)
public class Extrato  {

    private Conta conta;
    private long saldoInicial;
    
    private LocalDate dataInicial;
    private LocalDate dataFinal;
    
    private List<FatoDetalhe> fatos;

    public Extrato(
            final Conta conta,
            final long saldoInicial,
            final LocalDate dataInicial, 
            final LocalDate dataFinal, 
            final List<FatoDetalhe> fatos) {
        
        this(dataInicial,dataFinal,fatos);
        this.conta = conta;
        this.saldoInicial = saldoInicial;
    }
    
    
    public Extrato(
            final LocalDate dataInicial, 
            final LocalDate dataFinal, 
            final List<FatoDetalhe> fatos) {
        
        this.dataInicial = dataInicial;
        this.dataFinal = dataFinal;
        this.fatos = fatos;
    }

    public Conta getConta() {
        return conta;
    }

    public void setConta(Conta conta) {
        this.conta = conta;
    }

    public long getSaldoInicial() {
        return saldoInicial;
    }

    public void setSaldoInicial(long saldoInicial) {
        this.saldoInicial = saldoInicial;
    }
    
    public LocalDate getDataInicial() {
        return dataInicial;
    }

    public void setDataInicial(LocalDate dataInicial) {
        this.dataInicial = dataInicial;
    }

    public LocalDate getDataFinal() {
        return dataFinal;
    }

    public void setDataFinal(LocalDate dataFinal) {
        this.dataFinal = dataFinal;
    }

    public List<FatoDetalhe> getFatos() {
        return fatos;
    }

    public void setFatos(List<FatoDetalhe> fatos) {
        this.fatos = fatos;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 11)
            .append(getDataInicial())
            .append(getDataFinal())
            .append(getSaldoInicial())
            .append(getConta())
            .append(getFatos())
            .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        boolean equal = obj != null && (obj instanceof Extrato);
        if(equal){
            final Extrato that = (Extrato) obj;
            equal = 
                Objects.equals(this.getDataInicial(), that.getDataInicial())
                && Objects.equals(this.getDataFinal(), that.getDataFinal())
                && Objects.equals(this.getSaldoInicial(), that.getSaldoInicial())
                && Objects.equals(this.getConta(), that.getConta())
                && Objects.equals(this.getFatos(), that.getFatos());
        }
        return equal;
    }
}