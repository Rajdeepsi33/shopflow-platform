package com.shopflow.consumer.mapper;

import com.shopflow.consumer.dto.FulfilmentResponseDto;
import com.shopflow.consumer.entity.Fulfilment;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface FulfilmentMapper {

    FulfilmentResponseDto toResponse(Fulfilment entity);

    List<FulfilmentResponseDto> toResponseList(List<Fulfilment> entities);
}