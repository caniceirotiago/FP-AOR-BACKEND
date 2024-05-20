package aor.fpbackend.service;

import jakarta.ejb.EJB;
import jakarta.ws.rs.Path;

@Path("/interests")
public class InterestService {

    @EJB
    InterestBean interestBean;


}
