package com.sequenceiq.cloudbreak.repository;

import static com.sequenceiq.cloudbreak.authorization.OrganizationPermissions.Action.READ;

import java.util.Set;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.sequenceiq.cloudbreak.aspect.DisableHasPermission;
import com.sequenceiq.cloudbreak.aspect.organization.CheckPermissionsByOrganizationId;
import com.sequenceiq.cloudbreak.aspect.organization.OrganizationResourceType;
import com.sequenceiq.cloudbreak.authorization.OrganizationResource;
import com.sequenceiq.cloudbreak.domain.RDSConfig;
import com.sequenceiq.cloudbreak.service.EntityType;

@EntityType(entityClass = RDSConfig.class)
@Transactional(Transactional.TxType.REQUIRED)
@DisableHasPermission
@OrganizationResourceType(resource = OrganizationResource.RDS)
public interface RdsConfigRepository extends OrganizationResourceRepository<RDSConfig, Long> {

    @CheckPermissionsByOrganizationId(action = READ)
    @Query("SELECT r FROM RDSConfig r LEFT JOIN FETCH r.clusters WHERE r.organization.id = :orgId")
    Set<RDSConfig> findAll(@Param("orgId") Long orgId);

    @CheckPermissionsByOrganizationId(action = READ, organizationIdIndex = 1)
    @Query("SELECT r FROM RDSConfig r LEFT JOIN FETCH r.clusters WHERE r.name= :name AND r.organization.id = :orgId AND r.status = 'USER_MANAGED'")
    RDSConfig findByName(@Param("name") String name, @Param("orgId") Long orgId);

    @CheckPermissionsByOrganizationId(action = READ, organizationIdIndex = 1)
    @Query("SELECT r FROM RDSConfig r LEFT JOIN FETCH r.clusters WHERE r.id= :id AND r.status <> 'DEFAULT_DELETED' AND r.organization.id = :orgId")
    RDSConfig findById(@Param("id") Long id, @Param("orgId") Long orgId);

    @CheckPermissionsByOrganizationId(action = READ, organizationIdIndex = 1)
    @Query("SELECT r FROM RDSConfig r INNER JOIN r.clusters cluster LEFT JOIN FETCH r.clusters WHERE cluster.id= :clusterId "
            + "AND r.organization.id = :orgId")
    Set<RDSConfig> findByClusterId(@Param("clusterId") Long clusterId, @Param("orgId") Long orgId);

    @CheckPermissionsByOrganizationId(action = READ, organizationIdIndex = 1)
    @Query("SELECT r FROM RDSConfig r INNER JOIN r.clusters cluster LEFT JOIN FETCH r.clusters WHERE cluster.id= :clusterId "
            + "AND r.organization.id = :orgId AND r.status = 'USER_MANAGED'")
    Set<RDSConfig> findUserManagedByClusterId(@Param("clusterId") Long clusterId, @Param("orgId") Long orgId);

    @CheckPermissionsByOrganizationId(action = READ, organizationIdIndex = 2)
    @Query("SELECT r FROM RDSConfig r INNER JOIN r.clusters cluster WHERE cluster.id= :clusterId "
            + "AND r.organization.id = :orgId AND r.status <> 'DEFAULT_DELETED' AND r.type= :type")
    RDSConfig findByClusterIdAndType(@Param("clusterId") Long clusterId, @Param("type") String type, @Param("orgId") Long orgId);

    @CheckPermissionsByOrganizationId(action = READ)
    @Query("SELECT r FROM RDSConfig r WHERE r.organization.id = :orgId")
    Set<RDSConfig> listByOrganizationId(@Param("orgId") Long orgId);
}
