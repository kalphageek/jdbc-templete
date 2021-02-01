package me.kalpha.jdbctemplate.query;

import me.kalpha.jdbctemplate.common.ErrorsModel;
import me.kalpha.jdbctemplate.domain.ExtractResult;
import me.kalpha.jdbctemplate.domain.QueryDto;
import me.kalpha.jdbctemplate.domain.QueryResult;
import me.kalpha.jdbctemplate.index.IndexController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping(value = "/data")
public class QueryController {

    private final QueryService queryService;
    private final QueryValidator queryValidator;

    @Autowired
    public QueryController(QueryService queryService, QueryValidator queryValidator) {
        this.queryService = queryService;
        this.queryValidator = queryValidator;
    }

    /**
     * 샘플데이터를 가장 빠른 속도록 조회한다. 조건과 순서를 지정할 수 없다
     * @param queryDto Query할 테이블명, DBType (ORACLE / OTHERS)
     * @return 샘플데이터
     */
    @GetMapping("/table/samples")
    public ResponseEntity findSamples(@RequestBody QueryDto queryDto) {

        List<QueryResult> list =  queryService.findSamples(queryDto);

        // Hateoas (Link 및 Profile)
        CollectionModel<QueryResult> outputModel = CollectionModel.of(list);
        outputModel.add(Link.of("/docs/index.html#resources-table-samples").withRel("profile"))
                .add(linkTo(this.getClass()).slash("table/samples").withSelfRel())
                .add(linkTo(this.getClass()).slash("table").withRel("table-paging"))
                .add(linkTo(this.getClass()).slash("table").withRel("table-extract"));
        return ResponseEntity.ok().body(outputModel);
    }

    /**
     * 샘플데이터를 가장 빠른 속도록 조회한다. 조건과 순서를 지정할 수 없다
     * @param queryDto Query할 테이블명, DBType (ORACLE / OTHERS)
     * @return 추출 레코드 수
     */
    @PostMapping("/table")
    public ResponseEntity extractTable(@RequestBody QueryDto queryDto) {
        long extractCount = queryService.extractTable(queryDto);
        ExtractResult extractResult = new ExtractResult(extractCount);
        EntityModel<ExtractResult> entityModel = EntityModel.of(extractResult);
        entityModel.add(Link.of("/docs/index.html#resources-table-extract").withRel("profile"))
                .add(linkTo(this.getClass()).slash("table").withSelfRel())
                .add(linkTo(this.getClass()).slash("table").withRel("table-paging"))
                .add(linkTo(this.getClass()).slash("table/samples").withRel("table-samples"))
        ;
        return ResponseEntity.ok().body(entityModel);
    }

    /**
     * 샘플데이터를 가장 빠른 속도록 조회한다. 조건과 순서를 지정할 수 없다
     * @param pageable 페이지 정보 - size, offset 등
     * @param assembler 페이지 navigation 정보 - fist, prev, page, next, last
     * @param queryDto  Query할 테이블명, DBType (ORACLE / OTHERS)
     * @return 샘플데이터
     */
    @GetMapping("/table")
    public ResponseEntity findTable(Pageable pageable, PagedResourcesAssembler assembler, @RequestBody QueryDto queryDto) {
        Page<QueryResult> page =  queryService.findTable(pageable, queryDto);

        // Hateoas (Link 및 Profile)
        PagedModel pagedModel = assembler.toModel(page, r -> EntityModel.of((QueryResult) r));
        pagedModel.add(Link.of("/docs/index.html#resources-table-paging").withRel("profile"))
                .add(linkTo(this.getClass()).slash("table").withSelfRel())
                .add(linkTo(this.getClass()).slash("table").withRel("table-extract"))
                .add(linkTo(this.getClass()).slash("table/samples").withRel("table-samples"))
        ;
        return ResponseEntity.ok().body(pagedModel);
    }

    @GetMapping("/query/validate")
    public ResponseEntity validateQuery(@RequestBody QueryDto queryDto, Errors errors) {
        queryValidator.validateSql(queryDto, errors);
        if (errors.hasErrors()) {
            EntityModel errorsModel = ErrorsModel.modelOf(errors);
            errorsModel.add(Link.of("/doc/index.html#overview-errors").withRel("profile"))
                    .add(linkTo(IndexController.class).slash("/data/query").withRel("index"))
                    .add(linkTo(IndexController.class).slash("/data/table").withRel("index"));

            return ResponseEntity.badRequest().body(errorsModel);
        }
        return ResponseEntity.ok(true);
    }

    @GetMapping("/query")
    public ResponseEntity findByQuery(Pageable pageable, PagedResourcesAssembler assembler, @RequestBody QueryDto queryDto, Errors errors) {
        queryValidator.validateSql(queryDto, errors);
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(ErrorsModel.modelOf(errors));
        }
        Page<QueryResult> page = queryService.findByQuery(pageable, queryDto);

        // Hateoas (Link 및 Profile)
        PagedModel pagedModel = assembler.toModel(page, r -> PagedModel.of((QueryResult) r));
        pagedModel.add(Link.of("/docs/index.html#resources-query-paging").withRel("profile"))
                .add(linkTo(this.getClass()).slash("/query").withSelfRel())
                .add(linkTo(this.getClass()).slash("/query").withRel("query-extract"));

        return ResponseEntity.ok().body(pagedModel);
    }

    @PostMapping("/query")
    public ResponseEntity extractByQuery(@RequestBody QueryDto queryDto, Errors errors) {
        queryValidator.validateSql(queryDto, errors);
        if (errors.hasErrors()) {
            return ResponseEntity.badRequest().body(ErrorsModel.modelOf(errors));
        }

        long extractCount = queryService.extractByQuery(queryDto);
        ExtractResult extractResult = new ExtractResult(extractCount);

        //Hateoas
        EntityModel<ExtractResult> entityModel = EntityModel.of(extractResult);
        entityModel.add(Link.of("/docs/index.html#resources-query-extract").withRel("profile"))
                .add(linkTo(this.getClass()).slash("/query").withSelfRel())
                .add(linkTo(this.getClass()).slash("/query").withRel("query-paging"));

        return ResponseEntity.ok().body(entityModel);
    }
}