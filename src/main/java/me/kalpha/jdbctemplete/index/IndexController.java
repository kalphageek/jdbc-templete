package me.kalpha.jdbctemplete.index;


import me.kalpha.jdbctemplete.query.QueryController;
import org.springframework.hateoas.RepresentationModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
public class IndexController {
    @GetMapping
    public RepresentationModel index() {
        RepresentationModel index = new RepresentationModel();
        index.add(linkTo(QueryController.class).withRel("query"));
        return index;
    }
}