package com.school.exception;
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String msg) { super(msg); }
    public ResourceNotFoundException(String r, Long id) { super(r+" introuvable id="+id); }
}
