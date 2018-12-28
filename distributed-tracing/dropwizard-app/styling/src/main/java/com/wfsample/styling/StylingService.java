package com.wfsample.styling;

import com.wavefront.sdk.common.WavefrontSender;
import com.wavefront.sdk.common.application.ApplicationTags;
import com.wavefront.sdk.direct.ingestion.WavefrontDirectIngestionClient;
import com.wavefront.sdk.dropwizard.reporter.WavefrontDropwizardReporter;
import com.wfsample.common.BeachShirtsUtils;
import com.wfsample.common.DropwizardServiceConfig;
import com.wfsample.common.Tracing;
import com.wfsample.common.dto.PackedShirtsDTO;
import com.wfsample.common.dto.ShirtDTO;
import com.wfsample.common.dto.ShirtStyleDTO;
import com.wfsample.common.dto.DeliveryStatusDTO;
import com.wfsample.service.DeliveryApi;
import com.wfsample.service.StylingApi;

import io.opentracing.tag.Tags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import io.dropwizard.Application;
import io.dropwizard.setup.Environment;
import io.opentracing.Scope;
import io.opentracing.Tracer;

import static java.util.stream.Collectors.toList;

/**
 * Driver for styling service which manages different styles of shirts and takes orders for a shirts
 * of a given style.
 *
 * @author Srujan Narkedamalli (snarkedamall@wavefront.com).
 */
public class StylingService extends Application<DropwizardServiceConfig> {
  private static Logger logger = LoggerFactory.getLogger(StylingService.class);

  private final Tracer tracer;

  private StylingService(Tracer tracer) {
    this.tracer = tracer;
  }

  public static void main(String[] args) throws Exception {
    Tracer tracer = Tracing.init("styling");
    new StylingService(tracer).run(args);
  }

  @Override
  public void run(DropwizardServiceConfig configuration, Environment environment) {
    String deliveryUrl = "http://" + configuration.getDeliveryHost() + ":" + configuration
        .getDeliveryPort();
    environment.jersey().register(new StylingWebResource(
        BeachShirtsUtils.createProxyClient(deliveryUrl, DeliveryApi.class, this.tracer)));

    ApplicationTags applicationTags = new ApplicationTags.Builder("beachshirts", "styling").
            cluster("us-west-1").shard("primary").customTags(new HashMap<String, String>(){{
      put("env", "Staging");
      put("location", "SF");
    }}).build();

    WavefrontSender wfSender = new WavefrontDirectIngestionClient.Builder("https://tracing.wavefront.com",
            "104c7c31-598d-46e2-9972-0fd6c1ec8285").build();

    WavefrontDropwizardReporter wfDropwizardReporter =
            new WavefrontDropwizardReporter.Builder(environment.metrics(), applicationTags).build(wfSender);
    wfDropwizardReporter.start();
  }

  public class StylingWebResource implements StylingApi {
    // sample set of static styles.
    private List<ShirtStyleDTO> shirtStyleDTOS = new ArrayList<>();
    private final DeliveryApi deliveryApi;

    StylingWebResource(DeliveryApi deliveryApi) {
      this.deliveryApi = deliveryApi;
      ShirtStyleDTO dto = new ShirtStyleDTO();
      dto.setName("style1");
      dto.setImageUrl("style1Image");
      ShirtStyleDTO dto2 = new ShirtStyleDTO();
      dto2.setName("style2");
      dto2.setImageUrl("style2Image");
      shirtStyleDTOS.add(dto);
      shirtStyleDTOS.add(dto2);
    }

    @Override
    public List<ShirtStyleDTO> getAllStyles(HttpHeaders httpHeaders) {
      try (Scope scope = Tracing.startServerSpan(tracer, httpHeaders, "getAllStyles")) {
        return shirtStyleDTOS;
      }
    }

    @Override
    public Response makeShirts(String id, int quantity, HttpHeaders httpHeaders) {
      try (Scope scope = Tracing.startServerSpan(tracer, httpHeaders, "makeShirts")) {
        if (ThreadLocalRandom.current().nextInt(0, 5) == 0) {
          String msg = "Failed to make shirts!";
          logger.warn(msg);
          return Response.status(Response.Status.SERVICE_UNAVAILABLE).entity(msg).build();
        }
        String orderNum = UUID.randomUUID().toString();
        List<ShirtDTO> packedShirts = new ArrayList<>();
        for (int i = 0; i < quantity; i++) {
          packedShirts.add(new ShirtDTO(new ShirtStyleDTO(id, id + "Image")));
        }
        PackedShirtsDTO packedShirtsDTO = new PackedShirtsDTO(packedShirts.stream().
            map(shirt -> new ShirtDTO(
                new ShirtStyleDTO(shirt.getStyle().getName(), shirt.getStyle().getImageUrl()))).
            collect(toList()));
        Response deliveryResponse = deliveryApi.dispatch(orderNum, packedShirtsDTO, httpHeaders);
        if (deliveryResponse.getStatus() < 400) {
          return Response.ok().entity(deliveryResponse.readEntity(DeliveryStatusDTO.class)).build();
        } else {
          scope.span().setTag(Tags.ERROR.getKey(), true);
          String msg = "Failed to make shirts!";
          logger.warn(msg);
          return Response.status(deliveryResponse.getStatus()).entity(msg).build();
        }
      }
    }
  }
}