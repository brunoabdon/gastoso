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

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collection;

import com.fasterxml.jackson.core.JsonFactory;

import com.github.brunoabdon.gastoso.Conta;
import com.github.brunoabdon.gastoso.Fato;
import com.github.brunoabdon.gastoso.Lancamento;

import com.github.brunoabdon.gastoso.ext.FatoDetalhado;
import com.github.brunoabdon.gastoso.ext.Saldo;

/**
 *
 * @author Bruno Abdon
 */
class Serial {

    public static final JsonFactory JSON_FACT = new JsonFactory(); 
    
    public static final String 
        CONTA = "conta",
        CONTA_ID = "contaId",
        DESC = "desc",
        DESTINO = "destino",
        DESTINO_ID = "destinoId",
        DIA = "dia",
        FATO = "fato",
        FATO_ID = "fatoId",
        ID = "id",
        LANCAMENTOS = "lancamentos",
        NOME = "nome",
        ORIGEM = "origem",
        ORIGEM_ID = "origemId",
        VALOR = "valor";

    public static final String SALDO_CLASSNAME = Saldo.class.getName();
    public static final String CONTA_CLASSNAME = Conta.class.getName();
    public static final String FATO_CLASSNAME = Fato.class.getName();

    public static final String FATO_DETALHADO_CLASSNAME = 
        FatoDetalhado.class.getName();
    
    public static final String LANCAMENTO_CLASSNAME = 
        Lancamento.class.getName();
    
    public static boolean isAccecptable(
            final Class<?> type, 
            final Type genericType,
            final String ... acceptableClassNames){
        return 
            Arrays
                .stream(acceptableClassNames)
                .anyMatch(getRelevantTypeName(type, genericType)::contains);
    }

    public static String getRelevantTypeName(
            final Class<?> type, 
            final Type genericType) {
        return 
            getRelevantTypeName(
                Collection.class.isAssignableFrom(type),
                type, 
                genericType);
    }

    public static String getRelevantTypeName(
            final boolean isCollection, 
            final Class<?> type, 
            final Type genericType) {
        return (isCollection ? genericType : type).getTypeName();
    }


}