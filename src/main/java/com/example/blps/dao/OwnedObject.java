package com.example.blps.dao;

import com.example.blps.dao.repository.model.User;

public interface OwnedObject {
    User getOwner();
}
