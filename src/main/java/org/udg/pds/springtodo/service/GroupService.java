package org.udg.pds.springtodo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.udg.pds.springtodo.controller.exceptions.ServiceException;
import org.udg.pds.springtodo.entity.*;
import org.udg.pds.springtodo.repository.GroupRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class GroupService {

    @Autowired
    GroupRepository groupRepository;

    @Autowired
    protected UserService userService;

    public GroupRepository crud() {
        return groupRepository;
    }

    public Collection<Group> getGroups(Long id) {
        Optional<User> u = userService.crud().findById(id);
        if (!u.isPresent()) throw new ServiceException("User does not exists");

        Stream<Group> combinedStream = Stream.of(u.get().getOwner_groups(), u.get().getMember_groups())
            .flatMap(Collection::stream);

        return combinedStream.collect(Collectors.toList());
        //return Iterables.unmodifiableIterable(Iterables.concat(getOwnerGroups(id), getMemberGroups(id)));

    }

    public Collection<Group> getOwnerGroups(Long id) {
        Optional<User> u = userService.crud().findById(id);
        if (!u.isPresent()) throw new ServiceException("User does not exists");
        return u.get().getOwner_groups();
    }

    public Collection<Group> getMemberGroups(Long id) {
        Optional<User> u = userService.crud().findById(id);
        if (!u.isPresent()) throw new ServiceException("User does not exists");
        return u.get().getMember_groups();
    }

    public Group getGroup(Long userId, Long id) {
        Optional<Group> t = groupRepository.findById(id);
        if (!t.isPresent()) throw new ServiceException("Group does not exists");
        if (t.get().getOwnerId() != userId)
            throw new ServiceException("User does not own this group");
        return t.get();
    }

    @Transactional
    public IdObject addGroup(Long userId, String name, String description) {
        try {
            User user = userService.getUser(userId);
            Group group = new Group(name, description);

            group.setOwner(user);
            user.addOwner_group(group);
            groupRepository.save(group);

            return new IdObject(group.getId());

        } catch (Exception ex) {
            // Very important: if you want that an exception reaches the EJB caller, you have to throw an ServiceException
            // We catch the normal exception and then transform it in a ServiceException
            throw new ServiceException(ex.getMessage());
        }
    }

    @Transactional
    public void addMembersToGroup(Long userId, Long groupId, Collection<Long> members) {
        Group g = this.getGroup(userId, groupId);

        //if (g.getOwner().getId() != userId)
        if (g.getOwnerId() != userId)
            throw new ServiceException("This user is not the owner of the group");

        try {
            for (Long member : members) {
                Optional<User> user = userService.crud().findById(member);
                if (user.isPresent())
                    g.addMember(user.get());
                else
                    throw new ServiceException("Tag dos not exists");
            }
        } catch (Exception ex) {
            // Very important: if you want that an exception reaches the EJB caller, you have to throw an ServiceException
            // We catch the normal exception and then transform it in a ServiceException
            throw new ServiceException(ex.getMessage());
        }
    }

    @Transactional
    public void addMemberToGroup(Long userId, Long groupId, Long member) {
        Group g = this.getGroup(userId, groupId);

        //if (g.getOwner().getId() != userId)
        if (g.getOwnerId() != userId)
            throw new ServiceException("This user is not the owner of the group");

        try {
            Optional<User> user = userService.crud().findById(member);
            if (user.isPresent())
                g.addMember(user.get());
            else
                throw new ServiceException("Tag dos not exists");
        } catch (Exception ex) {
            // Very important: if you want that an exception reaches the EJB caller, you have to throw an ServiceException
            // We catch the normal exception and then transform it in a ServiceException
            throw new ServiceException(ex.getMessage());
        }
    }

    public Collection<User> getGroupMembers(Long userId, Long id) {
        Group g = this.getGroup(userId, id);
        User u = g.getOwner();

        if (u.getId() != userId)
            throw new ServiceException("Logged user does not own the group");

        return g.getMembers();
    }

}
