package com.seaman.model.response;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Setter
@Getter
public class CourseListResponse {
    private List<CoursesResponse> courses;
}
