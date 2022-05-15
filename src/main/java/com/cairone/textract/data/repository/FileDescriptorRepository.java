package com.cairone.textract.data.repository;

import java.util.UUID;

import org.springframework.data.repository.CrudRepository;

import com.cairone.textract.data.model.FileDescriptor;

public interface FileDescriptorRepository extends CrudRepository<FileDescriptor, UUID> {

}
