package com.civicbuild.service;

import com.civicbuild.entity.RevisionRequest;
import com.civicbuild.entity.User;

public interface RevisionService {
    RevisionRequest getRevisionById(Long id, User user);
    RevisionRequest submitRevision(Long id, String remarks, User user);
}
