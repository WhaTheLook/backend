package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Date;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String url;
    private Date date;
    private Boolean deleteYN;

    @ManyToOne
    @JoinColumn(name = "post_id")
    private Post post;
}
