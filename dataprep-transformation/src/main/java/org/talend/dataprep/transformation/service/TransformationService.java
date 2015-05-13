package org.talend.dataprep.transformation.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import javax.servlet.http.HttpServletResponse;

import au.com.bytecode.opencsv.CSVWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.WebApplicationContext;
import org.talend.dataprep.api.dataset.ColumnMetadata;
import org.talend.dataprep.api.dataset.DataSetMetadata;
import org.talend.dataprep.api.type.ExportType;
import org.talend.dataprep.api.type.Type;
import org.talend.dataprep.exception.CommonErrorCodes;
import org.talend.dataprep.exception.TDPException;
import org.talend.dataprep.exception.json.JsonErrorCodeDescription;
import org.talend.dataprep.metrics.Timed;
import org.talend.dataprep.metrics.VolumeMetered;
import org.talend.dataprep.transformation.api.action.metadata.ActionMetadata;
import org.talend.dataprep.transformation.api.transformer.Transformer;
import org.talend.dataprep.transformation.api.transformer.TransformerFactory;
import org.talend.dataprep.transformation.api.transformer.exporter.ExportConfiguration;
import org.talend.dataprep.transformation.api.transformer.exporter.ExportFactory;
import org.talend.dataprep.transformation.api.transformer.json.DiffTransformerFactory;
import org.talend.dataprep.transformation.api.transformer.json.SimpleTransformerFactory;
import org.talend.dataprep.transformation.exception.TransformationErrorCodes;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wordnik.swagger.annotations.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@Api(value = "transformations", basePath = "/transform", description = "Transformations on data")
public class TransformationService {

    @Autowired
    private WebApplicationContext context;

    @Autowired(required = true)
    private Jackson2ObjectMapperBuilder builder;

    @Autowired
    private ActionMetadata[] allActions;

//    private TransformerFactory getTransformerFactory() {
//        return context.getBean(SimpleTransformerFactory.class);
//    }
//
//    private TransformerFactory getDiffTransformerFactory() {
//        return context.getBean(DiffTransformerFactory.class);
//    }

    @Autowired
    private SimpleTransformerFactory simpleFactory;

    @Autowired
    private DiffTransformerFactory diffFactory;

    @Autowired
    private ExportFactory exportFactory;

    @RequestMapping(value = "/transform", method = POST, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Transform input data", notes = "This operation returns the input data transformed using the supplied actions.")
    @VolumeMetered
    public void transform(
            @ApiParam(value = "Actions to perform on content (encoded in Base64).") @RequestParam(value = "actions", defaultValue = "", required = false) String actions,
            @ApiParam(value = "Data set content as JSON") InputStream content, HttpServletResponse response) {
        try {
            final String decodedActions = new String(Base64.getDecoder().decode(actions));
            final Transformer transformer = simpleFactory.withActions(decodedActions).get();
            transformer.transform(content, response.getOutputStream());
        } catch (IOException e) {
            throw new TDPException(TransformationErrorCodes.UNABLE_TO_PARSE_JSON, e);
        }
    }

    @RequestMapping(value = "/transform/{format}", method = POST)
    @ApiOperation(value = "Transform input data", notes = "This operation export the input data transformed using the supplied actions in the provided format.")
    @VolumeMetered
    public void transform(
            @ApiParam(value = "Output format.") @PathVariable("format") final ExportType format,
            @ApiParam(value = "Actions to perform on content (encoded in Base64).") @RequestParam(value = "actions", defaultValue = "", required = false) final String actions,
            @ApiParam(value = "CSV separator.") @RequestParam(value = "separator", required = false) final String csvSeparator,
            @ApiParam(value = "Data set content as JSON") final InputStream content, HttpServletResponse response) {
        try {
            final String decodedActions = new String(Base64.getDecoder().decode(actions));
            final Character decodedCsvSeparator = csvSeparator != null ? new String(Base64.getDecoder().decode(csvSeparator)).charAt(0) : null;
            final ExportConfiguration configuration = ExportConfiguration.builder()
                    .format(format)
                    .csvSeparator(decodedCsvSeparator)
                    .actions(decodedActions)
                    .build();

            response.setContentType(format.getMimeType());

            final Transformer transformer = exportFactory.getExporter(configuration);
            transformer.transform(content, response.getOutputStream());
        } catch (IOException e) {
            throw new TDPException(TransformationErrorCodes.UNABLE_TO_PARSE_JSON, e);
        } catch(UnsupportedOperationException e) {
            if(format != null) {
                throw new TDPException(TransformationErrorCodes.OUTPUT_TYPE_NOT_SUPPORTED, e);
            }
            throw e;
        }
    }

    @RequestMapping(value = "/transform/preview", method = POST, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Transform input data", notes = "This operation returns the input data diff between the old and the new transformation actions")
    @VolumeMetered
    public void transformPreview(@ApiParam(value = "Old actions to perform on content (encoded in Base64).")
    @RequestParam(value = "oldActions", required = false)
    final String oldActions, @ApiParam(value = "New actions to perform on content (encoded in Base64).")
    @RequestParam(value = "newActions", required = false)
    final String newActions, @ApiParam(value = "The row indexes to return")
    @RequestParam(value = "indexes", required = false)
    final String indexes, @ApiParam(value = "Data set content as JSON")
    final InputStream content, final HttpServletResponse response) {
        try {
            final String decodedIndexes = indexes == null ? null : new String(Base64.getDecoder().decode(indexes));
            final String decodedOldActions = oldActions == null ? null : new String(Base64.getDecoder().decode(oldActions));
            final String decodedNewActions = newActions == null ? null : new String(Base64.getDecoder().decode(newActions));

            final Transformer transformer = diffFactory.withIndexes(decodedIndexes)
                    .withActions(decodedOldActions, decodedNewActions).get();
            transformer.transform(content, response.getOutputStream());
        } catch (IOException e) {
            throw new TDPException(TransformationErrorCodes.UNABLE_TO_PARSE_JSON, e);
        }
    }

    @RequestMapping(value = "/suggest/column", method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Suggest actions for a given column metadata", notes = "This operation returns an array of suggested actions in decreasing order of importance.")
    @ApiResponses({ @ApiResponse(code = 500, message = "Internal error") })
    public @ResponseBody List<ActionMetadata> suggest(@RequestBody(required = false) ColumnMetadata column) {
        if (column == null) {
            return Collections.emptyList();
        }
        String typeName = column.getType();
        Type type = Type.get(typeName);
        ArrayList<ActionMetadata> suggestedActions = new ArrayList<>();
        // look for all actions applicable to the column type
        for (ActionMetadata am : allActions) {
            Set<Type> compatibleColumnTypes = am.getCompatibleColumnTypes();
            for (Type columnType : compatibleColumnTypes) {
                if (columnType.isAssignableFrom(type)) {
                    suggestedActions.add(am);
                }
            }
        }
        return suggestedActions;
    }

    @RequestMapping(value = "/suggest/dataset", method = POST, consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Suggest actions for a given data set metadata", notes = "This operation returns an array of suggested actions in decreasing order of importance.")
    @ApiResponses({ @ApiResponse(code = 500, message = "Internal error") })
    public @ResponseBody List<ActionMetadata> suggest(InputStream dataset) {
        if (dataset == null) {
            return Collections.emptyList();
        }
        try {
            ObjectMapper objectMapper = builder.build();
            DataSetMetadata dataSetMetadata = objectMapper.reader(DataSetMetadata.class).readValue(dataset);
            // Temporary: no data set actions at this moment
            if (dataSetMetadata != null) {
                return Collections.emptyList();
            } else {
                return Collections.emptyList();
            }
        } catch (IOException e) {
            throw new TDPException(TransformationErrorCodes.UNABLE_TO_COMPUTE_DATASET_ACTIONS, e);
        }
    }

    /**
     * List all transformation related error codes.
     */
    @RequestMapping(value = "/transform/errors", method = RequestMethod.GET, produces = APPLICATION_JSON_VALUE)
    @ApiOperation(value = "Get all transformation related error codes.", notes = "Returns the list of all transformation related error codes.")
    @Timed
    public void listErrors(HttpServletResponse response) {
        try {
            // need to cast the typed dataset errors into mock ones to use json parsing
            List<JsonErrorCodeDescription> errors = new ArrayList<>(TransformationErrorCodes.values().length);
            for (TransformationErrorCodes code : TransformationErrorCodes.values()) {
                errors.add(new JsonErrorCodeDescription(code));
            }
            builder.build().writer().writeValue(response.getOutputStream(), errors);
        } catch (IOException e) {
            throw new TDPException(CommonErrorCodes.UNEXPECTED_EXCEPTION, e);
        }
    }
}
