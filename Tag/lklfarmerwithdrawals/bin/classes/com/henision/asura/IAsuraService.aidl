//This file is IAsuraService.aidl  
package com.henision.asura;  
interface IAsuraService  
{  
    void setInputMode(int mode);  
    void setAttribute(int x, int y, int w, int h);
    void registApp(String packedname);
}  