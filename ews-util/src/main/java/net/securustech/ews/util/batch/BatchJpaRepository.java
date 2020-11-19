package net.securustech.ews.util.batch;

import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

import static org.hibernate.cfg.AvailableSettings.STATEMENT_BATCH_SIZE;

public class BatchJpaRepository<T, ID> extends SimpleJpaRepository<T, ID> {
    private static final String DEFAULT_BATCH_SIZE = "500";

    private final EntityManager em;
    private int batchSize;

    private void setBatchSize() {
        this.batchSize = Integer.parseInt(
            (String)em.getEntityManagerFactory().getProperties().getOrDefault(STATEMENT_BATCH_SIZE, DEFAULT_BATCH_SIZE)
        );
    }

    public BatchJpaRepository(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);

        this.em = entityManager;
        setBatchSize();
    }

    public BatchJpaRepository(Class<T> domainClass, EntityManager em) {
        super(domainClass, em);

        this.em = em;
        setBatchSize();
    }

    @Transactional
    @Override
    public <S extends T> List<S> saveAll(Iterable<S> entities) {
        Assert.notNull(entities, "The given Iterable of entities not be null!");

        int i = 0;

        List<S> result = new ArrayList<>();

        for (S entity: entities) {
            em.persist(entity);
            result.add(entity);

            i++;

            if (i % batchSize == 0 && i > 0) {
                em.flush();
                em.clear();

                i = 0;
            }
        }

        if (i > 0) {
            em.flush();
            em.clear();
        }

        return result;
    }
}
