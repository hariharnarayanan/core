package net.securustech.ews.util.batch;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.stereotype.Repository;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "person")
class Employee {
    @Id
    private Long id;

    @Size(min = 3, max = 20)
    private String name;

    public Employee(Long id, @Size(min = 3, max = 20) String name) {
        this.id = id;
        this.name = name;
    }

    public Employee(@Size(min = 3, max = 20) String name) {
        this.name = name;
    }
}

@Repository
interface EmployeeRepository extends JpaRepository<Employee, Long> {
}

@RunWith(SpringRunner.class)
@DataJpaTest
@Ignore
public class BatchJpaRepositoryTest {
    @Autowired
    private EmployeeRepository employeeRepository;

    @SpringBootApplication
    @EnableJpaRepositories(repositoryBaseClass = BatchJpaRepository.class)
    public static class TestApplication {
        public static void main(String[] args) {
            SpringApplication.run(TestApplication.class, args);
        }
    }

    @Transactional
    @Test
    public void whenInsertingEmployees_thenEmployeesAreCreated() {
        Employee e1 = new Employee(1L,"Employee 1");
        Employee e2 = new Employee(2L,"Employee 2");
        Employee e3 = new Employee(3L,"Employee 3");
        Employee e4 = new Employee(4L,"Employee 4");
        List<Employee> employees = Arrays.asList(e1, e2, e3, e4);
        employeeRepository.saveAll(employees);

        assertThat(employeeRepository.count()).isEqualTo(employees.size());
    }
}
