package com.study.querydsl.entity;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class MemberTest {

    @PersistenceContext
    EntityManager em;

    @Test
    @Rollback(false)
    public void testEntity() {
        Team teamA = new Team("데이터 플랫폼 팀");
        Team teamB = new Team("인프라 팀");
        Team teamC = new Team("웹 개발 팀");
        Team teamD = new Team("APP 개발 팀");

        em.persist(teamA);
        em.persist(teamB);
        em.persist(teamC);
        em.persist(teamD);

        // teamA
        Member member1 = new Member("김영민", 30, teamA);
        Member member2 = new Member("원영식", 30, teamA);
        em.persist(member1);
        em.persist(member2);

        // teamB
        Member member3 = new Member("김진엽", 29, teamB);
        Member member4 = new Member("박진우", 29, teamB);
        Member member5 = new Member("임수현", 29, teamB);
        em.persist(member3);
        em.persist(member4);
        em.persist(member5);

        // teamD
        Member member6 = new Member("김주한", 19, teamD);
        em.persist(member6);

        // 초기화
        em.flush(); // DB 반영
        em.clear(); // 영속성 컨텍스트 초기화

        //---------------------------------------------------------------------------//

        // 01. 단일 회원 테스트
        Member findMemberByUserName = em.createQuery("select m from Member m where m.userName = :userName", Member.class)
                                .setParameter("userName", "김영민")
                                .getSingleResult();

//        assertThat(member1).isEqualTo(findMemberByUserName); // em.flush(), em.clear()를 사용하지 않고 비교 == 'true'
        assertThat(member1.getUserName()).isEqualTo(findMemberByUserName.getUserName());

        // 02. 전체 팀 조회
        List<Team> findTeam = em.createQuery("select t from Team t", Team.class).getResultList();
        assertThat(findTeam.size()).isEqualTo(4); // 팀이 4개가 맞는가?
//        assertThat(findTeam.size()).isEqualTo(5); // fail

        // 03. 전체 회원 조회
        /*List<Member> findMember = em.createQuery("select m from Member m", Member.class).getResultList();
        System.out.println("----------------------------------------------------");
        for (Member member : findMember) {
            System.out.println("[01: AAA] -> ✅ member = " + member);
            System.out.println("[02: BBB] ---> ⚡ member.team = " + member.getTeam()); // 지연로딩 -> 실제 team.get
            System.out.println();
        }
        System.out.println("----------------------------------------------------");*/
    }
}