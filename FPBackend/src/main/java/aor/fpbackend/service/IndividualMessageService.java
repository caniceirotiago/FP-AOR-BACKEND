package aor.fpbackend.service;

import aor.fpbackend.bean.IndividualMessageBean;
import jakarta.ejb.EJB;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("/individualMessage")
public class IndividualMessageService {
    @EJB
    IndividualMessageBean individualMessageBean;

    @POST
    @Path("/send")
    public void sendIndividualMessage() {
        individualMessageBean.sendIndividualMessage();
    }
}
