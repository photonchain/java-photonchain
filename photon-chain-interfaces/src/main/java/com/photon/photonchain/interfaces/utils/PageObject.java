package com.photon.photonchain.interfaces.utils;


public class PageObject {
	

     private Integer pageNumber;

     private Integer pageSize;

     private Integer firstRecord;
     
     
     public Integer getFirstRecord() {
    	 return  (pageNumber-1)*pageSize;
     }
     
 
     

    public Integer getFirstRecord(Integer sumRecord) {
    	if(sumRecord==0) {
    		return 0;
    	}

 		Integer sumPage =null;
 		if(sumRecord%pageSize==0) {
			sumPage = sumRecord/pageSize;
		}else {
			sumPage = sumRecord/pageSize+1;
		}

		if(pageNumber>sumPage) {
			pageNumber = sumPage;
		}
		if(pageNumber<1){
			pageNumber = 1;
		}
		 
    	return  (pageNumber-1)*pageSize;
     }
    
    
	public Integer getPageNumber() {
		return this.pageNumber < 1 ? 1 :this.pageNumber;
	}
	public void setPageNumber(Integer pageNumber) {
		this.pageNumber = pageNumber;
	}
	public Integer getPageSize() {
		return pageSize;
	}
	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}
     
     
}
