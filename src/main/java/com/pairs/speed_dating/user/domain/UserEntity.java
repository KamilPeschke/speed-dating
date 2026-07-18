package com.pairs.speed_dating.user.domain;

import com.pairs.speed_dating.user.Gender;
import com.pairs.speed_dating.user.UserStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name="users")
@Getter
@NoArgsConstructor
public class UserEntity {

  @Id
  @GeneratedValue
  private UUID id;

  @Column(nullable = false,  unique = true)
  private String email;

  @Column(nullable = false)
  private String password;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false)
  private String surname;

  @Column(nullable = false)
  private int age;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private UserStatus status;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Gender gender;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Gender interestedIn;

  @Column()
  private String profilePhotoLink;

//  @JdbcTypeCode(SqlTypes.JSON)
  @Column()
  private List<String> photos;

  @Column(nullable = false)
  private Date createdAt;

  @Column()
  private Date updatedAt;

  @Column()
  private Date deletedAt;

  @Builder
  public UserEntity(String email, String password, String name,  String surname, int age, Gender gender, Gender interestedIn, Date createdAt) {
    this.email = email;
    this.password = password;
    this.name = name;
    this.surname = surname;
    this.age = age;
    this.status = UserStatus.UNAVAILABLE;
    this.gender = gender;
    this.interestedIn = interestedIn;
    this.createdAt = createdAt;
  }

  public void changeStatus(UserStatus newStatus) {
    this.status = newStatus;
  }
}
