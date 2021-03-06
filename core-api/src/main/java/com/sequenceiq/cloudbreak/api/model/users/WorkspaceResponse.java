package com.sequenceiq.cloudbreak.api.model.users;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sequenceiq.cloudbreak.api.model.v2.WorkspaceStatus;
import com.sequenceiq.cloudbreak.doc.ModelDescriptions;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel
public class WorkspaceResponse extends WorkspaceBase {

    @ApiModelProperty(ModelDescriptions.ID)
    private Long id;

    private Set<UserWorkspacePermissionsJson> users;

    private WorkspaceStatus status;

    @JsonProperty("id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<UserWorkspacePermissionsJson> getUsers() {
        return users;
    }

    public void setUsers(Set<UserWorkspacePermissionsJson> users) {
        this.users = users;
    }

    public WorkspaceStatus getStatus() {
        return status;
    }

    public void setStatus(WorkspaceStatus status) {
        this.status = status;
    }

    public static class NameComparator implements Comparator<WorkspaceResponse>, Serializable {
        @Override
        public int compare(WorkspaceResponse o1, WorkspaceResponse o2) {
            return o1.getName().compareTo(o2.getName());
        }
    }
}
