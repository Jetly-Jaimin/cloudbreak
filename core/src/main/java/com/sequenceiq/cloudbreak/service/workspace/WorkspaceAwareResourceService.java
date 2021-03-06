package com.sequenceiq.cloudbreak.service.workspace;

import java.util.Set;

import com.sequenceiq.cloudbreak.authorization.WorkspaceResource;
import com.sequenceiq.cloudbreak.domain.workspace.User;
import com.sequenceiq.cloudbreak.domain.workspace.Workspace;
import com.sequenceiq.cloudbreak.domain.workspace.WorkspaceAwareResource;

public interface WorkspaceAwareResourceService<T extends WorkspaceAwareResource> {

    T createForLoggedInUser(T resource, Long workspaceId);

    T create(T resource, Long workspaceId, User user);

    T create(T resource, Workspace workspace, User user);

    T getByNameForWorkspaceId(String name, Long workspaceId);

    T getByNameForWorkspace(String name, Workspace workspace);

    Set<T> findAllByWorkspace(Workspace workspace);

    Set<T> findAllByWorkspaceId(Long workspaceId);

    T delete(T resource);

    Set<T> delete(Set<T> resources);

    T deleteByNameFromWorkspace(String name, Long workspaceId);

    Set<T> deleteMultipleByNameFromWorkspace(Set<String> names, Long workspaceId);

    WorkspaceResource resource();

    Iterable<T> findAll();

    T pureSave(T resource);

    Iterable<T> pureSaveAll(Iterable<T> resources);
}
