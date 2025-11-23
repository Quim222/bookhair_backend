package com.bookhair.backend.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.bookhair.backend.model.StatusUser;
import com.bookhair.backend.model.User;
import com.bookhair.backend.model.UserRole;
import com.bookhair.dto.UserDto.UserResponseDto;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
  User findByEmailIgnoreCase(String email);

  List<User> findAllByOrderByUserRoleAsc();

  boolean existsByEmailIgnoreCase(String email);

  void deleteByUserId(String userId);

  List<User> findByStatusUser(StatusUser statusUser);

  List<User> findByUserRole(UserRole userRole);

  @Query("""
        select new com.bookhair.dto.UserDto.UserResponseDto(
          u.userId,
          u.name,
          u.email,
          u.phone,
          u.userRole,
          u.statusUser,
          case when exists (
            select 1 from UserPhoto p where p.referenceId = u.userId
          ) then concat('/photosUser/', u.userId) else null end,
          exists (select 1 from UserPhoto p2 where p2.referenceId = u.userId),
          u.createdAt
        )
        from User u
        where (:role is null or u.userRole = :role)
      """)
  List<UserResponseDto> findUsersWithPhoto(@Param("role") UserRole role);
}
