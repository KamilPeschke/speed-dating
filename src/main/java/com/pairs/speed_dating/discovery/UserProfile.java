package com.pairs.speed_dating.discovery;

import com.pairs.speed_dating.user.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class UserProfile {
    private UUID userId;
    private String name;
    private int age;
    private Gender gender;
    private double distance;
    private String profilePhotoLink;
    private List<String> photos;

}
