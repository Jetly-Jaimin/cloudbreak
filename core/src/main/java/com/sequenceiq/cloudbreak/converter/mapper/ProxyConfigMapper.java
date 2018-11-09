package com.sequenceiq.cloudbreak.converter.mapper;

import java.util.Set;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import com.sequenceiq.cloudbreak.api.model.proxy.ProxyConfigRequest;
import com.sequenceiq.cloudbreak.api.model.proxy.ProxyConfigResponse;
import com.sequenceiq.cloudbreak.domain.ProxyConfig;

@Mapper(componentModel = "spring")
public interface ProxyConfigMapper {

    @Mappings({
            @Mapping(target = "owner", ignore = true),
            @Mapping(target = "account", ignore = true),
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "workspace", ignore = true),
            @Mapping(target = "environments", ignore = true),
            @Mapping(target = "userName", expression = "java(new com.sequenceiq.cloudbreak.domain.Secret(proxyConfigRequest.getUserName()))"),
            @Mapping(target = "password", expression = "java(new com.sequenceiq.cloudbreak.domain.Secret(proxyConfigRequest.getPassword()))")
    })
    ProxyConfig mapRequestToEntity(ProxyConfigRequest proxyConfigRequest);

    @Mappings(@Mapping(target = "environments", ignore = true))
    ProxyConfigResponse mapEntityToResponse(ProxyConfig proxyConfigRequest);

    Set<ProxyConfigResponse> mapEntityToResponse(Set<ProxyConfig> proxyConfigRequest);
}
