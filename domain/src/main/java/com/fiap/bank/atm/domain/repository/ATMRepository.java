package com.fiap.bank.atm.domain.repository;

import com.fiap.bank.atm.domain.model.BaseEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Contrato genérico básico para os repositórios do domínio.
 *
 * O tipo T precisa obrigatoriamente ser uma entidade que
 * estenda BaseEntity.
 *
 * @param <T> tipo da entidade manipulada pelo repositório
 */
public interface ATMRepository<T extends BaseEntity> {

    Optional<T> findById(UUID id);

    List<T> findAll();

    void save(T entity);

    void deleteById(UUID id);
}