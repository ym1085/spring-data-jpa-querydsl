package com.study.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.study.querydsl.entity.Hello;
import com.study.querydsl.entity.QHello;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Commit
class SpringDataJpaQuerydslApplicationTests {

	@PersistenceContext
	private EntityManager em;

	/**
	 * Test when application is loaded
	 */
	@Test
	void contextLoads() {
		// 객체 생성 : Hello Entity
		Hello hello = new Hello();
		em.persist(hello);

		// Querydsl 사용을 위해 JPAQueryFactory 생성
		JPAQueryFactory query = new JPAQueryFactory(em);
		QHello qHello = new QHello("h"); // variable: alias

		// Querydsl 사용
		Hello result = query
				.selectFrom(qHello)
				.fetchOne();

		// 검증 : 하나의 트랜잭션 단위 내에서 동일한 엔티티 반환을 보장해야 한다
		assertThat(result).isEqualTo(hello);
		assertThat(result.getId()).isEqualTo(hello.getId());
	}
}
