package com.authenticket.authenticket.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@EqualsAndHashCode(callSuper = true)
@Table(name = "app_user", schema = "dev")
public class User extends BaseEntity implements UserDetails{
    @Id
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    @Column(name = "user_id", nullable = false)
    private Integer userId;
    @Column(name = "name", nullable = false)
    private String name;
    @Column(name = "email", nullable = false, unique = true)
    private String email;
    @Column(name = "password", nullable = false)
    private String password;
    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;
    @Column(name = "profile_image")
    private String profileImage;
    @Column(name = "enabled")
    private Boolean enabled = false;
    @Getter
    private static String role = "USER";

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        SimpleGrantedAuthority authority =
                new SimpleGrantedAuthority("USER");
        return Collections.singletonList(authority);
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return enabled;
    }

    @Override
    public boolean isAccountNonLocked() {
        return enabled;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return enabled;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

//    @OneToMany(mappedBy = "user")
//    private List<Ticket> tickets = new ArrayList<>();
}

