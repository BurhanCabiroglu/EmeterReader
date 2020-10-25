package com.example.myapplication;



public class Separator {

    String read_data;

    public Separator(String read_data) {
        this.read_data = read_data;
    }

    public String separate(){
        int startIndex = read_data.indexOf("(");
        int lastIndex = read_data.lastIndexOf(")");
        String val1;

        if(startIndex==-1){
             val1 = read_data.substring(0,0);
        }else{
             val1 = read_data.substring(0,startIndex);
        }

        String result="";

        switch (val1){
            case "0.0.0":
                result="Sayac Numarası : " + read_data.substring(startIndex+1,lastIndex);
                break;
            case "0.9.1":
                result ="Saat : " + read_data.substring(startIndex+1,lastIndex);
                break;
            case "0.9.2":
                result = "Tarih : " +read_data.substring(startIndex+1,lastIndex);
                break;
            case "0.9.5":
                result = "Haftanın "+read_data.substring(startIndex+1,lastIndex)+". günü";
                break;
            case "1.6.0" :
                result = "Demant : "+read_data.substring(startIndex+1,startIndex+11)+" "+read_data.substring(startIndex+13,read_data.length()-1);
                break;
            case "1.8.0":
                result = "T toplam : "+read_data.substring(startIndex+1,lastIndex);
                break;
            case "1.8.1":
                result = "T1 : "+ read_data.substring(startIndex+1,lastIndex);
                break;
            case "1.8.2":
                result = "T2 : "+ read_data.substring(startIndex+1,lastIndex);
                break;
            case "1.8.3":
                result = "T3 : "+ read_data.substring(startIndex+1,lastIndex);
                break;
            case "1.8.4":
                result = "T4 : "+ read_data.substring(startIndex+1,lastIndex);
                break;
            case "96.1.3":
                result = "Üretim Tarihi : "+ read_data.substring(startIndex+1,lastIndex);
                break;
            case "96.2.5":
                result = "Kalibrasyon Tarihi : "+ read_data.substring(startIndex+1,lastIndex);
                break;
            case "96.6.1":
                result = "Pil Durumu : "+ read_data.substring(startIndex+1,lastIndex);
                break;
            case "96.70":
                result = "Gövde Açılma Tarihi : "+ read_data.substring(startIndex+1,lastIndex);
                break;
            case "96.71":
                result = "Son Klemens Kapağı Açılma Tarih-Saat : "+ read_data.substring(startIndex+1,startIndex+15)+" "+read_data.substring(startIndex+17,read_data.length()-1);
                break;
            case "1.6.0*1":
                result = "Max Aktif Güç : "+ read_data.substring(startIndex+1,startIndex+11)+" "+read_data.substring(startIndex+13,read_data.length()-1);
                break;
            case "1.6.0*2":
                result = "Max Aktif Güç 2 : "+ read_data.substring(startIndex+1,startIndex+11)+" "+read_data.substring(startIndex+13,read_data.length()-1);
                break;
            case "0.8.0":
                result = "En Yüksek Güç Ölçü Süresi : "+ read_data.substring(startIndex+1,lastIndex);
                break;
            case "1.8.1*1":
                result = "Aylık Enerji T1 Önceki Ay : "+ read_data.substring(startIndex+1,startIndex+11)+" "+read_data.substring(startIndex+13,read_data.length()-1);
                break;
            case "1.8.1*2":
                result = "Aylık Enerji T1 2 Önceki Ay : "+ read_data.substring(startIndex+1,lastIndex);
                break;
            case "1.8.2*1":
                result = "Aylık Enerji T2 Önceki Ay :  "+ read_data.substring(startIndex+1,lastIndex);
                break;
            case "1.8.2*2":
                result = "Aylık Enerji T2 2 Önceki Ay  : "+ read_data.substring(startIndex+1,lastIndex);
                break;
            case "1.8.3*1":
                result = "Aylık Enerji T3 Önceki Ay  : : "+ read_data.substring(startIndex+1,lastIndex);
                break;
            case "1.8.3*2":
                result = "Aylık Enerji T3 2 Önceki Ay  : "+ read_data.substring(startIndex+1,lastIndex);
                break;
            case "1.8.4*1":
                result = "Aylık Enerji T4 Önceki Ay  : "+ read_data.substring(startIndex+1,lastIndex);
                break;
            case "1.8.4*2":
                result = "Aylık Enerji T4 2 Önceki Ay  : "+ read_data.substring(startIndex+1,lastIndex);
                break;
            case "96.71*1":
                result = "Bir Ay Önceki Klemens : "+ read_data.substring(startIndex+1,startIndex+14)+read_data.substring(startIndex+16,read_data.length()-1);
                break;
            case "96.71*2":
                result = "İki Ay Önceki Klemens : : "+ read_data.substring(startIndex+1,startIndex+14)+read_data.substring(startIndex+16,read_data.length()-1);
                break;
            case "96.2.2":
                result = "Tarife Bilgi Değişikliği Tarihi : "+read_data.substring(startIndex+1,lastIndex);
                break;
            case "96.50":
                result = "Tarife Saatleri Haftaiçi : "+read_data.substring(startIndex+1,lastIndex);
                break;
            case "96.51":
                result = "Tarife Saatleri Cumartesi : "+read_data.substring(startIndex+1,lastIndex);
                break;
            case "96.52":
                result = "Tarife Saatleri Pazar : "+read_data.substring(startIndex+1,lastIndex);
                break;
            case "96.60":
                result = "Tarife Saatleri Haftaiçi : "+read_data.substring(startIndex+1,lastIndex);
                break;
            case "96.61":
                result = "Tarife Saatleri Cumartesi : "+read_data.substring(startIndex+1,lastIndex);
                break;
            case "96.62":
                result = "Tarife Saatleri Pazar : "+read_data.substring(startIndex+1,lastIndex);
                break;
            case "96.77.0*1":
                result = "Kesinti 1 : "+read_data.substring(startIndex+1,lastIndex);
                break;
            case "96.77.0*2":
                result = "Kesinti 2 : "+read_data.substring(startIndex+1,lastIndex);
                break;
            case "96.7.0":
                result = "3 Faz Uzun Kesinti Sayısı : "+read_data.substring(startIndex+1,lastIndex);
                break;
        }
        return result;
    }
}
