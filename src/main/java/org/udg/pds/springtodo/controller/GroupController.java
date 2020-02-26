package org.udg.pds.springtodo.controller;

import com.fasterxml.jackson.annotation.JsonView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.udg.pds.springtodo.controller.exceptions.ControllerException;
import org.udg.pds.springtodo.entity.*;
import org.udg.pds.springtodo.service.GroupService;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Collection;
import java.util.Date;

@RequestMapping(path="/groups")
@RestController
public class GroupController extends BaseController {

    @Autowired
    GroupService groupService;

    @GetMapping(path="/{gid}")
    public Group getGroup(HttpSession session, @PathVariable("gid") Long groupId) {
        Long userId = getLoggedUser(session);
        return groupService.getGroup(userId, groupId);
    }

    @JsonView(Views.Private.class)
    public Collection<Group> listAllGroups(HttpSession session,
                                           @RequestParam(value = "from", required = false) Date from) {

        Long userId = getLoggedUser(session);

        return groupService.getGroups(userId);
    }

    @GetMapping(path="/owned")
    @JsonView(Views.Private.class)
    public Collection<Group> listAllOwnerGroups(HttpSession session,
                                                @RequestParam(value = "from", required = false) Date from) {

        Long userId = getLoggedUser(session);

        return groupService.getOwnerGroups(userId);
    }

    @GetMapping(path="/membership")
    @JsonView(Views.Private.class)
    public Collection<Group> listAllMemberGroups(HttpSession session,
                                                 @RequestParam(value = "from", required = false) Date from) {
        Long userId = getLoggedUser(session);

        return groupService.getMemberGroups(userId);
    }

    @PostMapping(consumes = "application/json")
    public IdObject addGroup(HttpSession session, @Valid @RequestBody R_Group group) {

        Long userId = getLoggedUser(session);

        if (group.name == null) {
            throw new ControllerException("No name supplied");
        }
        if (group.description == null) {
            throw new ControllerException("No description supplied");
        }

        return groupService.addGroup(userId, group.name, group.description);
    }

    @DeleteMapping(path="/{gid}")
    public String deleteGroup(HttpSession session,
                              @PathVariable("gid") Long groupId) {

        getLoggedUser(session);
        groupService.crud().deleteById(groupId);

        return BaseController.OK_MESSAGE;
    }

    @PostMapping(path="/{id}/members")
    public String addMembers(@RequestBody Collection<Long> members, HttpSession session,
                             @PathVariable("id") Long groupId) {

        Long userId = getLoggedUser(session);
        groupService.addMembersToGroup(userId, groupId, members);

        return BaseController.OK_MESSAGE;
    }

    @PostMapping(path="/{gid}/members/{uid}")
    public String addMember(@PathVariable("uid")  Long member, HttpSession session,
                            @PathVariable("gid") Long groupId) {

        Long userId = getLoggedUser(session);
        groupService.addMemberToGroup(userId, groupId, member);

        return BaseController.OK_MESSAGE;
    }

    @GetMapping(path="/{id}/members")
    public Collection<User> getGroupMembers(HttpSession session,
                                            @PathVariable("id") Long groupId) {

        Long userId = getLoggedUser(session);
        return groupService.getGroupMembers(userId, groupId);
    }

    // Body de la paticío a la API
    static class R_Group {

        @NotNull
        public String name;

        @NotNull
        public String description;
    }

}
