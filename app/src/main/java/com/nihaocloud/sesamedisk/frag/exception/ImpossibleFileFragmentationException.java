package com.nihaocloud.sesamedisk.frag.exception;

@SuppressWarnings("serial")
public class ImpossibleFileFragmentationException extends Exception{
	public ImpossibleFileFragmentationException(String reason){
		super(reason);
	}
}