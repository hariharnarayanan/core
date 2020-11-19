package net.securustech.ews.data.elasticsearch.rest.extension;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Optional;


@Service
public class EwsElasticsearchService<S, T, ID extends Serializable> {

    @Autowired(required = false)
    private ElasticsearchRepository ewsElasticsearchRepository;

    /**
     * Saves a given entity. Use the returned instance for further operations as the save operation might have changed the
     * entity instance completely.
     *
     * @param entity must not be {@literal null}.
     * @return the saved entity will never be {@literal null}.
     */
    public <S extends T> S save(S entity) {
        ewsElasticsearchRepository.save(entity);
        return entity;
    }

    /**
     * Saves a given entity with spring retryable feature.
     * Use the returned instance for further operations as the save operation might have changed the
     * entity instance completely.
     *
     * @param entity must not be {@literal null}.
     * @return the saved entity will never be {@literal null}.
     */
    @Retryable(
            maxAttempts = 3,
            backoff = @Backoff(delay = 5000)
    )
    public <S extends T> S saveDurable(S entity) {
        ewsElasticsearchRepository.save(entity);
        return entity;
    }

    /**
     * Deletes the entity with the given id.
     *
     * @param id must not be {@literal null}.
     * @throws IllegalArgumentException in case the given {@code id} is {@literal null}
     */
    public void deleteById(ID id) {
        ewsElasticsearchRepository.deleteById(id);
    }

    /**
     * Deletes the entity with the given id with the spring retryable feature.
     *
     * @param id must not be {@literal null}.
     * @throws IllegalArgumentException in case the given {@code id} is {@literal null}
     */
    @Retryable(
            maxAttempts = 3,
            backoff = @Backoff(delay = 5000)
    )
    public void deleteByIdDurable(ID id) {
        ewsElasticsearchRepository.deleteById(id);
    }

    /**
     * Retrieves an entity by its id.
     *
     * @param id must not be {@literal null}.
     * @return the entity with the given id or {@literal Optional#empty()} if none found
     * @throws IllegalArgumentException if {@code id} is {@literal null}.
     */
    public Optional<T> findById(ID id) {
        return ewsElasticsearchRepository.findById(id);
    }


}
