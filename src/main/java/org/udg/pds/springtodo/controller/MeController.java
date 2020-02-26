package org.udg.pds.springtodo.controller;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.udg.pds.springtodo.entity.*;
import org.udg.pds.springtodo.service.GroupService;

import javax.servlet.http.HttpSession;
import java.util.Collection;
import java.util.Date;

@RequestMapping(path="/me")
@RestController
public class MeController extends BaseController {

    @Autowired
    GroupService groupService;

    @GetMapping(path="/groups")
    @JsonView(Views.Private.class)
    public Collection<Group> listAllGroups(HttpSession session,
                                           @RequestParam(value = "from", required = false) Date from) {

        Long userId = getLoggedUser(session);

        return groupService.getGroups(userId);
    }
}
