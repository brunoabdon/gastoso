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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.Provider;
import javax.ws.rs.ext.ReaderInterceptor;
import javax.ws.rs.ext.ReaderInterceptorContext;

import br.nom.abdon.gastoso.Conta;
import br.nom.abdon.gastoso.Fato;
import br.nom.abdon.gastoso.Lancamento;
import br.nom.abdon.gastoso.ext.FatoDetalhado;

/**
 *
 * @author Bruno Abdon
 */
@Provider
public class ContasCacheReaderInterceptor implements ReaderInterceptor {

    private static final Logger LOG = 
        Logger.getLogger(ContasCacheReaderInterceptor.class.getName());
    
    private final Map<Integer,Conta> cacheContas = new HashMap<>();

    private final ReadWriteLock rwlock = new ReentrantReadWriteLock();

    private final static Function<FatoDetalhado, Stream<Lancamento>> FL_STREAM = 
        f-> f.getLancamentos().stream();

    @Override
    public Object aroundReadFrom(final ReaderInterceptorContext context) 
            throws IOException, WebApplicationException {
        
        Object entity = context.proceed();
        
        final Class<?> type = context.getType();
        
        if(Collection.class.isAssignableFrom(type)){
            final Type genericType = context.getGenericType();

            final ParameterizedType pt = (ParameterizedType)genericType;
            final Type t = pt.getActualTypeArguments()[0];
            final Class entityClass = 
                (t instanceof Class) 
                    ? (Class)t 
                    : (Class)((ParameterizedType)t).getRawType();

            final Stream<Lancamento> streamLancamento = 
                getStreamLancamento(entityClass, entity);
            
            if(streamLancamento != null){
                rwlock.readLock().lock();
                try {
                    streamLancamento.forEach(
                        lancamento -> {
                            final Integer key = lancamento.getConta().getId();
                            final Conta contaFull = cacheContas.get(key);
                            lancamento.setConta(contaFull);
                        }
                    );
                } finally {
                    rwlock.readLock().unlock();
                }
            }
        }
        return entity;
    }

    private Stream<Lancamento> getStreamLancamento(
            final Class entityClass, 
            final Object entity) {
        
        final Stream<Lancamento> streamLancamento;
        
        if(Lancamento.class.isAssignableFrom(entityClass)){
            
            final Collection<Lancamento> lancamentos =
                    (Collection<Lancamento>) entity;
            
            streamLancamento = lancamentos.stream();
            
        } else if(Fato.class.isAssignableFrom(entityClass)){
            
            final Collection<FatoDetalhado> fatos =
                    (Collection<FatoDetalhado>) entity;
            
            streamLancamento = fatos.stream().flatMap(FL_STREAM);
        } else {
            
            streamLancamento = null;
            
        }
        return streamLancamento;
    }

    public void updateContas(final Collection<Conta> contas){
        safelyModifyCache(
            cache -> {   
                cache.clear(); 
                contas.forEach(c -> cache.put(c.getId(), c));
            }
        );
    }

    public void updateConta(final Conta conta){
        safelyModifyCache(c -> c.put(conta.getId(), conta));
    }

    public void removeConta(final Integer idConta){
        safelyModifyCache(c -> c.remove(idConta));
    }

    private void safelyModifyCache(final Consumer<Map<Integer,Conta>> mod){
        rwlock.writeLock().lock();
        try {
            mod.accept(cacheContas);
        } finally {
            rwlock.writeLock().unlock();
        }
    }
}