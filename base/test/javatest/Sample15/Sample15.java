import java.io.*;
import com.FTSafe.Dongle;
public class Sample15
{//改示例需要一把子锁
    public Sample15()
	{
	}
    public static void main(final String args[]) throws IOException
    {
	   byte [] dongleInfo = new byte [100];
	   int [] count = new int[1];
	   long [] handle = new long [1];
	   int nRet = 0;
	   int i = 0;
	   
       Dongle dongle = new Dongle();	   
	   //枚举锁
       nRet = dongle.Dongle_Enum(dongleInfo, count);
	   if(nRet != Dongle.DONGLE_SUCCESS)
	   {
	     System.out.printf("Dongle_Enum error. error code: 0x%08X .\n ", nRet);
		 return ;
	   }	   
	   System.out.printf("Enum Dongle ARM count: [%d] .\n", count[0]); 
	   
	   //打开第一把锁
	   nRet = dongle.Dongle_Open(handle, 0);
	   if(nRet != Dongle.DONGLE_SUCCESS)
	   {
	     System.out.printf("Dongle_Open error. error code: 0x%08X .\n ", nRet);
		 return ;
	   }
	   System.out.printf("Open Dongle ARM success[handle=0x%08X]. \n",handle[0]);
	   
	   //验证开发商密码
	   int []nRemain = new int[1];
       String strPin = "FFFFFFFFFFFFFFFF"; //默认开发商密码
	   nRet = dongle.Dongle_VerifyPIN(handle[0], dongle.FLAG_ADMINPIN, strPin, nRemain);
	   if(nRet != Dongle.DONGLE_SUCCESS)
	   {
	     System.out.printf("Dongle_VerifyPIN error [remain cout: %d]. error code: 0x%08X .\n ", nRemain[0], nRet);
		 dongle.Dongle_Close(handle[0]);
		 return ;
	   }
	   System.out.printf("Verify admin pin success. \n");
	   
	   //读取RSA公私钥文件
	   byte []rsaPubkey = new byte[1024];
	   byte []rsaPrikey = new byte[1024];
	   int rsaPubLen = 0;
	   int rsaPriLen = 0;
	   
	   File f1 =  new File("update.Rsapub");	   
	   rsaPubLen = (int)f1.length();
	   FileInputStream fis = new FileInputStream(f1);	   
	   fis.read(rsaPubkey, 0, rsaPubLen);
	   fis.close();
	   
	   
	   File f2 = new File("update.Rsapri");
	   rsaPriLen = (int)f2.length();
	   fis = new FileInputStream(f2);
	   fis.read(rsaPrikey, 0, rsaPriLen);	   
	   fis.close();
	   
	   //设置远程升级私钥 在这里不是必须的。
	   FileOutputStream fos;
       fos = new FileOutputStream("update.RyArmUdp");
       	   
	   //制作升级包
	   byte[] inBuffer = new byte[1024];
	   byte[] outBuffer = new byte[1024];
	   int [] inBufferLen = new int[1];
	   int [] outBufferLen = new int[1];
	   int nFunc = Dongle.UPDATE_FUNC_CreateFile;
	   //1.升级创建文件
	   byte []dataLic = new byte[1024];
	   int []dataLicLen = new int[1];
	   outBufferLen[0] = 1024;
	   nRet = dongle.Convert_DATA_LIC_To_Buffer((short)0, (short)1, dataLic, dataLicLen);
	   nRet = dongle.Convert_DATA_FILE_ATTR_To_Buffer(1024, dataLic, dataLicLen[0], inBuffer, inBufferLen);
	   nRet = dongle.Dongle_MakeUpdatePacket(handle[0], null,nFunc,Dongle.FILE_DATA,0x3366,0,inBuffer,inBufferLen[0],rsaPubkey,outBuffer, outBufferLen);
	   if(nRet != Dongle.DONGLE_SUCCESS)
	   {
	     System.out.printf("Dongle_MakeUpdatePacket [CreateFile]error code: 0x%08X .\n ", nRet);
		 dongle.Dongle_Close(handle[0]);
		 return ;
	   }
	   System.out.printf("Make update packet [CreateFile] success. \n");
	   //写到文件中
	   fos.write(outBuffer, 0, outBufferLen[0]);
	   
	   //2.升级写文件
	   inBuffer = new byte[1024];
	   outBuffer = new byte[2048];
	   inBufferLen[0] = 1024;
	   outBufferLen[0] = 2048;
	   nFunc = Dongle.UPDATE_FUNC_WriteFile;
	   for(i = 0 ; i < 1024; i++) inBuffer[i] = (byte)0x55;
	   nRet = dongle.Dongle_MakeUpdatePacket(handle[0], null,nFunc,Dongle.FILE_DATA,0x3366,0,inBuffer,inBufferLen[0],rsaPubkey,outBuffer, outBufferLen);
	   if(nRet != Dongle.DONGLE_SUCCESS)
	   {
	     System.out.printf("Dongle_MakeUpdatePacket [WriteFile] error code: 0x%08X .\n ", nRet);
		 dongle.Dongle_Close(handle[0]);
		 return ;
	   }
	   System.out.printf("Make update packet [WriteFile] success. \n");
	   //写到文件中
	   fos.write(outBuffer, 0, outBufferLen[0]);
	   
	   //3.升级文件授权
	   inBuffer = new byte[1024];
	   outBuffer = new byte[1024];
	   inBufferLen[0] = 1024;
	   outBufferLen[0] = 1024;
	   nFunc = Dongle.UPDATE_FUNC_FileLic;
	   nRet = dongle.Convert_DATA_LIC_To_Buffer((short)0, (short)1, inBuffer, inBufferLen);
	   nRet = dongle.Dongle_MakeUpdatePacket(handle[0], null,nFunc,Dongle.FILE_DATA,0x3366,0,inBuffer,inBufferLen[0],rsaPubkey,outBuffer, outBufferLen);
	   if(nRet != Dongle.DONGLE_SUCCESS)
	   {
	     System.out.printf("Dongle_MakeUpdatePacket [FileLic] error code: 0x%08X .\n ", nRet);
		 dongle.Dongle_Close(handle[0]);
		 return ;
	   }
	   System.out.printf("Make update packet [FileLic] success. \n");
	   //写到文件中
	   fos.write(outBuffer, 0, outBufferLen[0]);
	   
	   //4.升级删除文件
	   outBuffer = new byte[1024];
	   outBufferLen[0] = 1024;
	   nFunc = Dongle.UPDATE_FUNC_DeleteFile;
	   nRet = dongle.Dongle_MakeUpdatePacket(handle[0], null,nFunc,Dongle.FILE_DATA,0x3366,0,null,0,rsaPubkey,outBuffer, outBufferLen);
	   if(nRet != Dongle.DONGLE_SUCCESS)
	   {
	     System.out.printf("Dongle_MakeUpdatePacket [DeleteFile] error code:0x%08X .\n ", nRet);
		 dongle.Dongle_Close(handle[0]);
		 return ;
	   }
	   System.out.printf("Make update packet [DeleteFile] success. \n");
	   //写到文件中
	   fos.write(outBuffer, 0, outBufferLen[0]);
	   
	   //5.升级下载可执行程序，本例中只升级一个可执行文件
	   inBuffer = new byte[1024];
	   outBuffer = new byte[4096];
	   outBufferLen[0] = 4096;
	   nFunc = Dongle.UPDATE_FUNC_DownloadExe;
	   nRet = dongle.Add_EXE_FILE_INFO_To_Buffer(inBuffer, inBufferLen, 0, (short)0x1234, (short)1776, (byte)0, g_progExeFile1);//添加一个文件就行
	   nRet = dongle.Dongle_MakeUpdatePacket(handle[0], null,nFunc,Dongle.FILE_EXE,0,0,inBuffer,inBufferLen[0],rsaPubkey,outBuffer, outBufferLen);
	   if(nRet != Dongle.DONGLE_SUCCESS)
	   {
	     System.out.printf("Dongle_MakeUpdatePacket [DownloadExeFile] error code:0x%08X .\n ", nRet);
		 dongle.Dongle_Close(handle[0]);
		 return ;
	   }
	   nRet = dongle.Clear_EXE_FILE_INFO_Buffer(inBuffer, 1);
	   System.out.printf("Make update packet [DownloadExeFile] success. \n");
	   //写到文件中
	   fos.write(outBuffer, 0, outBufferLen[0]);
	   
	   //6.升级解锁用户pin码，恢复到12345678，这个操作必须绑定硬件序列号
	   String shid = "1122334455667788";
	   outBuffer = new byte[1024];
	   outBufferLen[0] = 1024;
	   nFunc = Dongle.UPDATE_FUNC_UnlockUserPin;
	   nRet = dongle.Dongle_MakeUpdatePacket(handle[0], shid,nFunc,0,0,0,null,0,rsaPubkey,outBuffer, outBufferLen);
	   if(nRet != Dongle.DONGLE_SUCCESS)
	   {
	     System.out.printf("Dongle_MakeUpdatePacket [UnlockUserPin] error code:0x%08X .\n ", nRet);
		 dongle.Dongle_Close(handle[0]);
		 return ;
	   }
	   nRet = dongle.Clear_EXE_FILE_INFO_Buffer(inBuffer, 1);
	   System.out.printf("Make update packet [UnlockUserPin] success. \n");
	   //写到文件中
	   fos.write(outBuffer, 0, outBufferLen[0]);

       //7.升级到期时间	
       int time = 24;//升级使用24小时
	   inBuffer[0] = (byte) (time & 0xff);  
       inBuffer[1] = (byte) (time >> 8 & 0xff);  
       inBuffer[2] = (byte) (time >> 16 & 0xff);  
       inBuffer[3] = (byte) (time >> 24 & 0xff);  
	   outBuffer = new byte[1024];
	   outBufferLen[0] = 1024;
	   nFunc = Dongle.UPDATE_FUNC_Deadline;
	   nRet = dongle.Dongle_MakeUpdatePacket(handle[0], null,nFunc,0,0,0,inBuffer, 4 ,rsaPubkey,outBuffer, outBufferLen);
	   if(nRet != Dongle.DONGLE_SUCCESS)
	   {
	     System.out.printf("Dongle_MakeUpdatePacket [Deadline] error code:0x%08X .\n ", nRet);
		 dongle.Dongle_Close(handle[0]);
		 return ;
	   }
	   nRet = dongle.Clear_EXE_FILE_INFO_Buffer(inBuffer, 1);
	   System.out.printf("Make update packet [Deadline] success. \n");
	   //写到文件中
	   fos.write(outBuffer, 0, outBufferLen[0]);
	   
	   //7.使用种子码使用次数	
       int ncount = 1000;//使用1000次
	   //先转化为byte[]型
	   inBuffer[0] = (byte) (ncount & 0xff);  
       inBuffer[1] = (byte) (ncount >> 8 & 0xff);  
       inBuffer[2] = (byte) (ncount >> 16 & 0xff);  
       inBuffer[3] = (byte) (ncount >> 24 & 0xff);  
	   outBuffer = new byte[1024];
	   outBufferLen[0] = 1024;
	   nFunc = Dongle.UPDATE_FUNC_SeedCount;
	   nRet = dongle.Dongle_MakeUpdatePacket(handle[0], null,nFunc,0,0,0,inBuffer, 4 ,rsaPubkey,outBuffer, outBufferLen);
	   if(nRet != Dongle.DONGLE_SUCCESS)
	   {
	     System.out.printf("Dongle_MakeUpdatePacket [SeedCount] error code:0x%08X .\n ", nRet);
		 dongle.Dongle_Close(handle[0]);
		 return ;
	   }
	   nRet = dongle.Clear_EXE_FILE_INFO_Buffer(inBuffer, 1);
	   System.out.printf("Make update packet [SeedCount] success. \n");
	   //写到文件中
	   fos.write(outBuffer, 0, outBufferLen[0]);
	   
	   
	   //关闭加密锁
	   nRet = dongle.Dongle_Close(handle[0]);
	   if(nRet != Dongle.DONGLE_SUCCESS)
	   {
	      System.out.printf("Dongle_Close error. error code: 0x%08X \n", nRet);
		  return;
	   }
	   System.out.printf("Close Dongle ARM success. \n");
	   
	   fos.close();
       	   
    }
    public static byte [] g_progExeFile1= new byte[]{
	(byte)0x00,(byte)0x0c,(byte)0x00,(byte)0x68,(byte)0x55,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x59,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x5b,(byte)0x00,
	(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
	(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
	(byte)0x00,(byte)0x00,(byte)0x5d,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,
	(byte)0x5f,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x61,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x03,(byte)0x48,(byte)0x85,(byte)0x46,(byte)0x00,(byte)0xf0,
	(byte)0x22,(byte)0xfb,(byte)0x00,(byte)0x48,(byte)0x00,(byte)0x47,(byte)0xb1,(byte)0x03,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x0c,(byte)0x00,(byte)0x68,
	(byte)0x03,(byte)0x48,(byte)0x00,(byte)0x47,(byte)0xfe,(byte)0xe7,(byte)0xfe,(byte)0xe7,(byte)0xfe,(byte)0xe7,(byte)0xfe,(byte)0xe7,(byte)0xfe,(byte)0xe7,
	(byte)0x00,(byte)0x00,(byte)0x41,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x10,(byte)0xb5,(byte)0xd2,(byte)0x48,(byte)0x00,(byte)0xf0,(byte)0x00,(byte)0xfa,
	(byte)0xd0,(byte)0x48,(byte)0x10,(byte)0x21,(byte)0x00,(byte)0x1d,(byte)0x00,(byte)0xf0,(byte)0xf1,(byte)0xf9,(byte)0x10,(byte)0xbd,(byte)0x10,(byte)0xb5,
	(byte)0xcd,(byte)0x48,(byte)0x00,(byte)0xf0,(byte)0xda,(byte)0xf9,(byte)0x02,(byte)0x20,(byte)0x00,(byte)0xf0,(byte)0xe0,(byte)0xf9,(byte)0x10,(byte)0xbd,
	(byte)0x1c,(byte)0xb5,(byte)0x01,(byte)0x20,(byte)0x80,(byte)0x02,(byte)0x00,(byte)0x90,(byte)0x00,(byte)0x20,(byte)0x69,(byte)0x46,(byte)0x88,(byte)0x80,
	(byte)0xc8,(byte)0x80,(byte)0x08,(byte)0x23,(byte)0x6a,(byte)0x46,(byte)0xc5,(byte)0x49,(byte)0x01,(byte)0x20,(byte)0x00,(byte)0xf0,(byte)0x96,(byte)0xf9,
	(byte)0x1c,(byte)0xbd,(byte)0x10,(byte)0xb5,(byte)0xc2,(byte)0x49,(byte)0x01,(byte)0x20,(byte)0x00,(byte)0xf0,(byte)0xb8,(byte)0xf9,(byte)0x10,(byte)0xbd,
	(byte)0x38,(byte)0xb5,(byte)0x88,(byte)0x22,(byte)0x80,(byte)0x21,(byte)0xbd,(byte)0x48,(byte)0x00,(byte)0xf0,(byte)0xd1,(byte)0xfa,(byte)0xbc,(byte)0x4b,
	(byte)0xbd,(byte)0x4c,(byte)0x00,(byte)0x93,(byte)0x80,(byte)0x23,(byte)0x00,(byte)0x22,(byte)0x21,(byte)0x46,(byte)0x01,(byte)0x20,(byte)0x00,(byte)0xf0,
	(byte)0x9a,(byte)0xf9,(byte)0x09,(byte)0x21,(byte)0x09,(byte)0x03,(byte)0x88,(byte)0x42,(byte)0x09,(byte)0xd1,(byte)0x80,(byte)0x21,(byte)0xb5,(byte)0x48,
	(byte)0x00,(byte)0xf0,(byte)0xc7,(byte)0xfa,(byte)0xb3,(byte)0x4b,(byte)0x80,(byte)0x22,(byte)0x00,(byte)0x21,(byte)0x20,(byte)0x46,(byte)0x00,(byte)0xf0,
	(byte)0x7e,(byte)0xf9,(byte)0x38,(byte)0xbd,(byte)0x10,(byte)0xb5,(byte)0xaf,(byte)0x48,(byte)0x00,(byte)0xf0,(byte)0xdf,(byte)0xf9,(byte)0x20,(byte)0x21,
	(byte)0xad,(byte)0x48,(byte)0x00,(byte)0xf0,(byte)0xb8,(byte)0xfa,(byte)0xac,(byte)0x48,(byte)0x20,(byte)0x30,(byte)0x00,(byte)0xf0,(byte)0xce,(byte)0xf9,
	(byte)0x10,(byte)0xbd,(byte)0x10,(byte)0xb5,(byte)0x88,(byte)0xb0,(byte)0x10,(byte)0x20,(byte)0x05,(byte)0x90,(byte)0x00,(byte)0x20,(byte)0xaa,(byte)0x4c,
	(byte)0x06,(byte)0x90,(byte)0x08,(byte)0x23,(byte)0x05,(byte)0xaa,(byte)0x21,(byte)0x46,(byte)0x04,(byte)0x20,(byte)0x00,(byte)0xf0,(byte)0x57,(byte)0xf9,
	(byte)0x09,(byte)0x21,(byte)0x09,(byte)0x03,(byte)0x88,(byte)0x42,(byte)0x0b,(byte)0xd1,(byte)0x10,(byte)0x21,(byte)0x01,(byte)0xa8,(byte)0x00,(byte)0xf0,
	(byte)0x93,(byte)0xf9,(byte)0x01,(byte)0xab,(byte)0x00,(byte)0x93,(byte)0x10,(byte)0x23,(byte)0x00,(byte)0x22,(byte)0x21,(byte)0x46,(byte)0x04,(byte)0x20,
	(byte)0x00,(byte)0xf0,(byte)0x61,(byte)0xf9,(byte)0x08,(byte)0xb0,(byte)0x10,(byte)0xbd,(byte)0x70,(byte)0xb5,(byte)0x99,(byte)0x49,(byte)0x10,(byte)0x22,
	(byte)0x88,(byte)0x18,(byte)0x05,(byte)0x46,(byte)0x00,(byte)0xf0,(byte)0x6a,(byte)0xfa,(byte)0x99,(byte)0x4c,(byte)0x00,(byte)0x22,(byte)0x23,(byte)0x46,
	(byte)0x10,(byte)0x21,(byte)0x28,(byte)0x46,(byte)0x00,(byte)0xf0,(byte)0x28,(byte)0xfa,(byte)0x93,(byte)0x48,(byte)0x29,(byte)0x46,(byte)0x20,(byte)0x30,
	(byte)0x10,(byte)0x22,(byte)0x05,(byte)0x46,
(byte)0x00,(byte)0xf0,(byte)0x5c,(byte)0xfa,(byte)0x23,(byte)0x46,(byte)0x01,(byte)0x22,(byte)0x10,(byte)0x21,(byte)0x28,(byte)0x46,(byte)0x00,(byte)0xf0,(byte)0x1b,(byte)0xfa,
(byte)0x70,(byte)0xbd,(byte)0x70,(byte)0xb5,(byte)0x8b,(byte)0x49,(byte)0x10,(byte)0x22,(byte)0x88,(byte)0x18,(byte)0x05,(byte)0x46,(byte)0x00,(byte)0xf0,(byte)0x4e,(byte)0xfa,
(byte)0x8b,(byte)0x4c,(byte)0x00,(byte)0x22,(byte)0x23,(byte)0x46,(byte)0x10,(byte)0x21,(byte)0x28,(byte)0x46,(byte)0x00,(byte)0xf0,(byte)0x18,(byte)0xfa,(byte)0x85,(byte)0x48,
(byte)0x29,(byte)0x46,(byte)0x20,(byte)0x30,(byte)0x10,(byte)0x22,(byte)0x05,(byte)0x46,(byte)0x00,(byte)0xf0,(byte)0x40,(byte)0xfa,(byte)0x23,(byte)0x46,(byte)0x01,(byte)0x22,
(byte)0x10,(byte)0x21,(byte)0x28,(byte)0x46,(byte)0x00,(byte)0xf0,(byte)0x0b,(byte)0xfa,(byte)0x70,(byte)0xbd,(byte)0x7e,(byte)0x4a,(byte)0x10,(byte)0xb5,(byte)0x20,(byte)0x32,
(byte)0x20,(byte)0x21,(byte)0x50,(byte)0x1a,(byte)0x00,(byte)0xf0,(byte)0x0f,(byte)0xfa,(byte)0x10,(byte)0xbd,(byte)0x7a,(byte)0x4a,(byte)0x10,(byte)0xb5,(byte)0x20,(byte)0x32,
(byte)0x20,(byte)0x21,(byte)0x50,(byte)0x1a,(byte)0x00,(byte)0xf0,(byte)0x11,(byte)0xfa,(byte)0x10,(byte)0xbd,(byte)0xf0,(byte)0xb5,(byte)0xb7,(byte)0xb0,(byte)0x03,(byte)0x20,
(byte)0x69,(byte)0x46,(byte)0x88,(byte)0x84,(byte)0xff,(byte)0x20,(byte)0x01,(byte)0x30,(byte)0xc8,(byte)0x84,(byte)0x00,(byte)0x20,(byte)0xc0,(byte)0x43,(byte)0x74,(byte)0x4f,
(byte)0x00,(byte)0x26,(byte)0x0a,(byte)0x90,(byte)0x0c,(byte)0x23,(byte)0x09,(byte)0xaa,(byte)0x39,(byte)0x46,(byte)0x0b,(byte)0x96,(byte)0x03,(byte)0x20,(byte)0x00,(byte)0xf0,
(byte)0xe9,(byte)0xf8,(byte)0x09,(byte)0x25,(byte)0x2d,(byte)0x03,(byte)0x6b,(byte)0x4c,(byte)0xa8,(byte)0x42,(byte)0x03,(byte)0xd0,(byte)0x20,(byte)0x80,(byte)0xa7,(byte)0x80,
(byte)0x37,(byte)0xb0,(byte)0xf0,(byte)0xbd,(byte)0x0c,(byte)0xa9,(byte)0x38,(byte)0x46,(byte)0x00,(byte)0xf0,(byte)0x83,(byte)0xf9,(byte)0xa8,(byte)0x42,(byte)0x02,(byte)0xd0,
(byte)0x20,(byte)0x80,(byte)0x68,(byte)0x49,(byte)0x1a,(byte)0xe0,(byte)0x20,(byte)0x21,(byte)0x01,(byte)0xa8,(byte)0x00,(byte)0xf0,(byte)0x17,(byte)0xf9,(byte)0x68,(byte)0x46,
(byte)0x06,(byte)0x71,(byte)0x36,(byte)0xab,(byte)0x00,(byte)0x93,(byte)0x26,(byte)0xab,(byte)0x20,(byte)0x22,(byte)0x01,(byte)0xa9,(byte)0x38,(byte)0x46,(byte)0x00,(byte)0xf0,
(byte)0x7a,(byte)0xf9,(byte)0xa8,(byte)0x42,(byte)0x02,(byte)0xd0,(byte)0x20,(byte)0x80,(byte)0x5f,(byte)0x49,(byte)0x07,(byte)0xe0,(byte)0x26,(byte)0xab,(byte)0x20,(byte)0x22,
(byte)0x01,(byte)0xa9,(byte)0x15,(byte)0xa8,(byte)0x00,(byte)0xf0,(byte)0x7e,(byte)0xf9,(byte)0x5c,(byte)0x49,(byte)0x20,(byte)0x80,(byte)0xa1,(byte)0x80,(byte)0xd7,(byte)0xe7,
(byte)0xf0,(byte)0xb5,(byte)0xb7,(byte)0xb0,(byte)0x03,(byte)0x20,(byte)0x69,(byte)0x46,(byte)0x88,(byte)0x84,(byte)0x81,(byte)0x20,(byte)0x00,(byte)0x02,(byte)0xc8,(byte)0x84,
(byte)0x00,(byte)0x20,(byte)0xc0,(byte)0x43,(byte)0x53,(byte)0x4f,(byte)0x00,(byte)0x26,(byte)0x0a,(byte)0x90,(byte)0x0c,(byte)0x23,(byte)0x09,(byte)0xaa,(byte)0x39,(byte)0x46,
(byte)0x0b,(byte)0x96,(byte)0x03,(byte)0x20,(byte)0x00,(byte)0xf0,(byte)0xa6,(byte)0xf8,(byte)0x09,(byte)0x25,(byte)0x2d,(byte)0x03,(byte)0x49,(byte)0x4c,(byte)0xa8,(byte)0x42,
(byte)0x02,(byte)0xd0,(byte)0x20,(byte)0x80,(byte)0x79,(byte)0x10,(byte)0x23,(byte)0xe0,(byte)0x0c,(byte)0xa9,(byte)0x38,(byte)0x46,(byte)0x00,(byte)0xf0,(byte)0x66,(byte)0xf9,
(byte)0xa8,(byte)0x42,(byte)0x02,(byte)0xd0,(byte)0x20,(byte)0x80,(byte)0xa7,(byte)0x80,(byte)0xb2,(byte)0xe7,(byte)0x20,(byte)0x21,(byte)0x01,(byte)0xa8,(byte)0x00,(byte)0xf0,
(byte)0xd5,(byte)0xf8,(byte)0x68,(byte)0x46,(byte)0x06,(byte)0x71,(byte)0x36,(byte)0xab,(byte)0x00,(byte)0x93,(byte)0x26,(byte)0xab,(byte)0x20,(byte)0x22,(byte)0x01,(byte)0xa9,
(byte)0x38,(byte)0x46,(byte)0x00,(byte)0xf0,(byte)0x5d,(byte)0xf9,(byte)0xa8,(byte)0x42,(byte)0x02,(byte)0xd0,(byte)0x20,(byte)0x80,(byte)0x3e,(byte)0x49,(byte)0x07,(byte)0xe0,
(byte)0x26,(byte)0xab,(byte)0x20,(byte)0x22,(byte)0x01,(byte)0xa9,(byte)0x15,(byte)0xa8,(byte)0x00,(byte)0xf0,(byte)0x61,(byte)0xf9,(byte)0x3b,(byte)0x49,(byte)0x20,(byte)0x80,
(byte)0xa1,(byte)0x80,(byte)0x95,(byte)0xe7,(byte)0xf0,(byte)0xb5,(byte)0xff,(byte)0xb0,(byte)0x8c,(byte)0xb0,(byte)0x09,(byte)0xa8,(byte)0x00,(byte)0x90,(byte)0x08,(byte)0x90,
(byte)0x37,(byte)0x48,(byte)0x06,(byte)0x90,(byte)0x07,(byte)0x46,(byte)0xff,(byte)0x30,(byte)0x21,(byte)0x30,(byte)0x07,(byte)0x90,(byte)0x02,(byte)0x20,(byte)0x69,(byte)0x46,
(byte)0x08,(byte)0x81,(byte)0x80,(byte)0x02,(byte)0x48,(byte)0x81,(byte)0x00,(byte)0x20,(byte)0xc0,(byte)0x43,(byte)0x03,(byte)0x90,(byte)0x00,(byte)0x20,(byte)0x2e,(byte)0x4e,
(byte)0x04,(byte)0x90,(byte)0x20,(byte)0x37,(byte)0x0c,(byte)0x23,(byte)0x02,(byte)0xaa,(byte)0x31,(byte)0x46,(byte)0x02,(byte)0x20,(byte)0x00,(byte)0xf0,(byte)0x5a,(byte)0xf8,
(byte)0x09,(byte)0x25,(byte)0x2d,(byte)0x03,(byte)0x23,(byte)0x4c,(byte)0xa8,(byte)0x42,(byte)0x02,(byte)0xd0,(byte)0x20,(byte)0x80,(byte)0x24,(byte)0x49,(byte)0x07,(byte)0xe0,
(byte)0x30,(byte)0x46,(byte)0x00,(byte)0x99,(byte)0x00,(byte)0xf0,(byte)0xc9,(byte)0xf8,(byte)0xa8,(byte)0x42,(byte)0x05,(byte)0xd0,(byte)0x21,(byte)0x49,(byte)0x20,(byte)0x80,
(byte)0xa1,(byte)0x80,(byte)0x7f,(byte)0xb0,(byte)0x0c,(byte)0xb0,(byte)0xf0,(byte)0xbd,(byte)0x20,(byte)0x21,(byte)0x06,(byte)0x98,(byte)0x00,(byte)0xf0,(byte)0x86,(byte)0xf8,
(byte)0x20,(byte)0x22,(byte)0x18,(byte)0x48,(byte)0x06,(byte)0x99,(byte)0x00,(byte)0xf0,(byte)0x69,(byte)0xf9,(byte)0x00,(byte)0x23,(byte)0x05,(byte)0xaa,(byte)0x01,(byte)0x93,
(byte)0x00,(byte)0x92,(byte)0x3b,(byte)0x46,(byte)0x20,(byte)0x22,(byte)0x30,(byte)0x46,(byte)0x06,(byte)0x99,(byte)0x00,(byte)0xf0,(byte)0xb8,(byte)0xf8,(byte)0xa8,(byte)0x42,
(byte)0x02,(byte)0xd0,(byte)0x20,(byte)0x80,(byte)0xa6,(byte)0x80,(byte)0xe4,(byte)0xe7,(byte)0x01,(byte)0x23,(byte)0x05,(byte)0xaa,(byte)0x01,(byte)0x93,(byte)0x00,(byte)0x92,
(byte)0x68,(byte)0x46,(byte)0x81,(byte)0x8a,(byte)0x38,(byte)0x46,(byte)0x07,(byte)0x9b,(byte)0x08,(byte)0x9a,(byte)0x00,(byte)0xf0,(byte)0xb9,(byte)0xf8,(byte)0x04,(byte)0x46,
(byte)0x08,(byte)0x48,(byte)0x20,(byte)0x22,(byte)0x20,(byte)0x30,(byte)0x07,(byte)0x99,(byte)0x00,(byte)0xf0,(byte)0x48,(byte)0xf9,(byte)0x05,(byte)0x49,(byte)0x0b,(byte)0x48,
(byte)0x40,(byte)0x31,(byte)0x0c,(byte)0x80,(byte)0x88,(byte)0x80,(byte)0x68,(byte)0x46,(byte)0x80,(byte)0x8a,(byte)0x08,(byte)0x81,(byte)0x20,(byte)0x46,(byte)0xc8,(byte)0xe7,
(byte)0x5a,(byte)0xe6,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x68,(byte)0x34,(byte)0x12,(byte)0x00,(byte)0x00,(byte)0xff,(byte)0xff,(byte)0x00,(byte)0x00,
(byte)0x11,(byte)0x11,(byte)0x00,(byte)0x00,(byte)0x22,(byte)0x22,(byte)0x00,(byte)0x00,(byte)0x33,(byte)0x33,(byte)0x00,(byte)0x00,(byte)0x44,(byte)0x44,(byte)0x00,(byte)0x00,
(byte)0x00,(byte)0x0c,(byte)0x00,(byte)0x68,(byte)0x70,(byte)0xb5,(byte)0x93,(byte)0x4c,(byte)0x10,(byte)0x25,(byte)0x25,(byte)0x80,(byte)0xa0,(byte)0x80,(byte)0xe1,(byte)0x80,
(byte)0x91,(byte)0x48,(byte)0xe3,(byte)0x60,(byte)0xa2,(byte)0x60,(byte)0x80,(byte)0x47,(byte)0x60,(byte)0x88,(byte)0x70,(byte)0xbd,(byte)0x70,(byte)0xb5,(byte)0x8d,(byte)0x4c,
(byte)0x11,(byte)0x25,(byte)0x25,(byte)0x80,(byte)0x01,(byte)0x25,(byte)0xa5,(byte)0x80,(byte)0xe0,(byte)0x80,(byte)0x21,(byte)0x81,(byte)0x62,(byte)0x81,(byte)0x8a,(byte)0x48,
(byte)0xe3,(byte)0x60,(byte)0x80,(byte)0x47,(byte)0x60,(byte)0x88,(byte)0x70,(byte)0xbd,(byte)0x70,(byte)0xb5,(byte)0x86,(byte)0x4c,(byte)0x12,(byte)0x26,(byte)0x04,(byte)0x9d,
(byte)0x26,(byte)0x80,(byte)0xa0,(byte)0x80,(byte)0xe1,(byte)0x80,(byte)0x22,(byte)0x81,(byte)0x63,(byte)0x81,(byte)0x83,(byte)0x48,(byte)0xe5,(byte)0x60,(byte)0x80,(byte)0x47,
(byte)0x60,(byte)0x88,(byte)0x70,(byte)0xbd,(byte)0x10,(byte)0xb5,(byte)0x7f,(byte)0x4c,(byte)0x13,(byte)0x22,(byte)0x22,(byte)0x80,(byte)0xa0,(byte)0x80,(byte)0x7e,(byte)0x48,
(byte)0xe1,(byte)0x80,(byte)0x80,(byte)0x47,(byte)0x60,(byte)0x88,(byte)0x10,(byte)0xbd,(byte)0x10,(byte)0xb5,(byte)0x7a,(byte)0x4c,(byte)0x14,(byte)0x21,(byte)0x21,(byte)0x80,
(byte)0x60,(byte)0x60,(byte)0x79,(byte)0x48,(byte)0x80,(byte)0x47,(byte)0x60,(byte)0x88,(byte)0x10,(byte)0xbd,(byte)0x10,(byte)0xb5,(byte)0x75,(byte)0x4c,(byte)0x15,(byte)0x21,
(byte)0x21,(byte)0x80,(byte)0x20,(byte)0x71,(byte)0x74,(byte)0x48,(byte)0x80,(byte)0x47,(byte)0x60,(byte)0x88,(byte)0x10,(byte)0xbd,(byte)0x10,(byte)0xb5,(byte)0x71,(byte)0x4c,
(byte)0x16,(byte)0x22,(byte)0x22,(byte)0x80,(byte)0x60,(byte)0x60,(byte)0x70,(byte)0x48,(byte)0x21,(byte)0x72,(byte)0x80,(byte)0x47,(byte)0x60,(byte)0x88,(byte)0x10,(byte)0xbd,
(byte)0x10,(byte)0xb5,(byte)0x6c,(byte)0x4c,(byte)0x17,(byte)0x21,(byte)0x21,(byte)0x80,(byte)0x60,(byte)0x60,(byte)0x6b,(byte)0x48,(byte)0x80,(byte)0x47,(byte)0x60,(byte)0x88,
(byte)0x10,(byte)0xbd,(byte)0x10,(byte)0xb5,(byte)0x67,(byte)0x4c,(byte)0x18,(byte)0x21,(byte)0x21,(byte)0x80,(byte)0x60,(byte)0x60,(byte)0x66,(byte)0x48,(byte)0x80,(byte)0x47,
(byte)0x60,(byte)0x88,(byte)0x10,(byte)0xbd,(byte)0x10,(byte)0xb5,(byte)0x63,(byte)0x4c,(byte)0x29,(byte)0x21,(byte)0x21,(byte)0x80,(byte)0x60,(byte)0x60,(byte)0x62,(byte)0x48,
(byte)0x80,(byte)0x47,(byte)0x60,(byte)0x88,(byte)0x10,(byte)0xbd,(byte)0x10,(byte)0xb5,(byte)0x5e,(byte)0x4c,(byte)0x19,(byte)0x21,(byte)0x21,(byte)0x80,(byte)0x60,(byte)0x60,
(byte)0x5d,(byte)0x48,(byte)0x80,(byte)0x47,(byte)0x60,(byte)0x88,(byte)0x10,(byte)0xbd,(byte)0x10,(byte)0xb5,(byte)0x5a,(byte)0x4c,(byte)0x1a,(byte)0x21,(byte)0x21,(byte)0x80,
(byte)0x60,(byte)0x60,(byte)0x59,(byte)0x48,(byte)0x80,(byte)0x47,(byte)0x60,(byte)0x88,(byte)0x10,(byte)0xbd,(byte)0x10,(byte)0xb5,(byte)0x55,(byte)0x4c,(byte)0x1b,(byte)0x22,
(byte)0x22,(byte)0x80,(byte)0xa0,(byte)0x80,(byte)0x54,(byte)0x48,(byte)0xa1,(byte)0x60,(byte)0x80,(byte)0x47,(byte)0x60,(byte)0x88,(byte)0x10,(byte)0xbd,(byte)0xf8,(byte)0xb5,
(byte)0x50,(byte)0x4c,(byte)0x1c,(byte)0x25,(byte)0x07,(byte)0x9f,(byte)0x06,(byte)0x9e,(byte)0x25,(byte)0x80,(byte)0xa3,(byte)0x60,(byte)0x61,(byte)0x60,(byte)0xa2,(byte)0x81,
(byte)0xe0,(byte)0x81,(byte)0x4d,(byte)0x48,(byte)0x27,(byte)0x82,(byte)0x80,(byte)0x47,(byte)0xa0,(byte)0x89,(byte)0x30,(byte)0x80,(byte)0x60,(byte)0x88,(byte)0xf8,(byte)0xbd,
(byte)0xf8,(byte)0xb5,(byte)0x48,(byte)0x4c,(byte)0x1d,(byte)0x25,(byte)0x07,(byte)0x9f,(byte)0x06,(byte)0x9e,(byte)0x25,(byte)0x80,(byte)0xa3,(byte)0x60,(byte)0x60,(byte)0x60,
(byte)0x21,(byte)0x82,(byte)0xe2,(byte)0x60,(byte)0x44,(byte)0x48,(byte)0x67,(byte)0x82,(byte)0x80,(byte)0x47,(byte)0x20,(byte)0x8a,(byte)0x30,(byte)0x80,(byte)0x60,(byte)0x88,
(byte)0xf8,(byte)0xbd,(byte)0x10,(byte)0xb5,(byte)0x3f,(byte)0x4c,(byte)0x1e,(byte)0x22,(byte)0x22,(byte)0x80,(byte)0xa0,(byte)0x80,(byte)0x3e,(byte)0x48,(byte)0xa1,(byte)0x60,
(byte)0x80,(byte)0x47,(byte)0x60,(byte)0x88,(byte)0x10,(byte)0xbd,(byte)0x70,(byte)0xb5,(byte)0x3a,(byte)0x4c,(byte)0x1f,(byte)0x26,(byte)0x04,(byte)0x9d,(byte)0x26,(byte)0x80,
(byte)0xa3,(byte)0x60,(byte)0x61,(byte)0x60,(byte)0xa2,(byte)0x81,(byte)0xe0,(byte)0x81,(byte)0x37,(byte)0x48,(byte)0x80,(byte)0x47,(byte)0xa0,(byte)0x89,(byte)0x28,(byte)0x80,
(byte)0x60,(byte)0x88,(byte)0x70,(byte)0xbd,(byte)0x70,(byte)0xb5,(byte)0x33,(byte)0x4c,(byte)0x20,(byte)0x25,(byte)0x25,(byte)0x80,(byte)0xa1,(byte)0x60,(byte)0x60,(byte)0x60,
(byte)0x22,(byte)0x82,(byte)0x31,(byte)0x48,(byte)0xe3,(byte)0x60,(byte)0x80,(byte)0x47,(byte)0x60,(byte)0x88,(byte)0x70,(byte)0xbd,(byte)0x10,(byte)0xb5,(byte)0x2d,(byte)0x4c,
(byte)0x21,(byte)0x22,(byte)0x22,(byte)0x80,(byte)0xa0,(byte)0x80,(byte)0x2c,(byte)0x48,(byte)0xa1,(byte)0x60,(byte)0x80,(byte)0x47,(byte)0x60,(byte)0x88,(byte)0x10,(byte)0xbd,
(byte)0x70,(byte)0xb5,(byte)0x28,(byte)0x4c,(byte)0x22,(byte)0x26,(byte)0x04,(byte)0x9d,(byte)0x26,(byte)0x80,(byte)0xa3,(byte)0x60,(byte)0x61,(byte)0x60,(byte)0xa2,(byte)0x81,
(byte)0xe0,(byte)0x81,(byte)0x25,(byte)0x48,(byte)0x80,(byte)0x47,(byte)0xa0,(byte)0x89,(byte)0x28,(byte)0x80,(byte)0x60,(byte)0x88,(byte)0x70,(byte)0xbd,(byte)0x70,(byte)0xb5,
(byte)0x20,(byte)0x4c,(byte)0x23,(byte)0x25,(byte)0x25,(byte)0x80,(byte)0xa1,(byte)0x60,(byte)0x60,(byte)0x60,(byte)0x22,(byte)0x82,(byte)0x1e,(byte)0x48,(byte)0xe3,(byte)0x60,
(byte)0x80,(byte)0x47,(byte)0x60,(byte)0x88,(byte)0x70,(byte)0xbd,(byte)0x70,(byte)0xb5,(byte)0x1a,(byte)0x4c,(byte)0x24,(byte)0x25,(byte)0x25,(byte)0x80,(byte)0xa1,(byte)0x60,
(byte)0x60,(byte)0x60,(byte)0xa2,(byte)0x81,(byte)0x18,(byte)0x48,(byte)0xe3,(byte)0x81,(byte)0x80,(byte)0x47,(byte)0x60,(byte)0x88,(byte)0x70,(byte)0xbd,(byte)0x70,(byte)0xb5,
(byte)0x14,(byte)0x4c,(byte)0x25,(byte)0x25,(byte)0x25,(byte)0x80,(byte)0xa1,(byte)0x60,(byte)0x60,(byte)0x60,(byte)0xa2,(byte)0x81,(byte)0x12,(byte)0x48,(byte)0xe3,(byte)0x81,
(byte)0x80,(byte)0x47,(byte)0x60,(byte)0x88,(byte)0x70,(byte)0xbd,(byte)0x10,(byte)0xb5,(byte)0x0e,(byte)0x4c,(byte)0x26,(byte)0x23,(byte)0x23,(byte)0x80,(byte)0x23,(byte)0x1d,
(byte)0x07,(byte)0xc3,(byte)0x0d,(byte)0x48,(byte)0x80,(byte)0x47,(byte)0x60,(byte)0x88,(byte)0x10,(byte)0xbd,(byte)0x10,(byte)0xb5,(byte)0x09,(byte)0x4c,(byte)0x27,(byte)0x23,
(byte)0x23,(byte)0x80,(byte)0x23,(byte)0x1d,(byte)0x07,(byte)0xc3,(byte)0x08,(byte)0x48,(byte)0x80,(byte)0x47,(byte)0x60,(byte)0x88,(byte)0x10,(byte)0xbd,(byte)0x10,(byte)0xb5,
(byte)0x04,(byte)0x4c,(byte)0x28,(byte)0x23,(byte)0x23,(byte)0x80,(byte)0x23,(byte)0x1d,(byte)0x07,(byte)0xc3,(byte)0x03,(byte)0x48,(byte)0x80,(byte)0x47,(byte)0x60,(byte)0x88,
(byte)0x10,(byte)0xbd,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x14,(byte)0x00,(byte)0x68,(byte)0x01,(byte)0x7f,(byte)0x00,(byte)0x00,(byte)0x70,(byte)0xb5,(byte)0x06,(byte)0x46,
(byte)0x08,(byte)0x46,(byte)0x15,(byte)0x46,(byte)0x00,(byte)0xf0,(byte)0x0c,(byte)0xf8,(byte)0x34,(byte)0x46,(byte)0x03,(byte)0xe0,(byte)0x01,(byte)0x78,(byte)0x40,(byte)0x1c,
(byte)0x21,(byte)0x70,(byte)0x64,(byte)0x1c,(byte)0x6d,(byte)0x1e,(byte)0xf9,(byte)0xd2,(byte)0x30,(byte)0x46,(byte)0x70,(byte)0xbd,(byte)0xee,(byte)0xe7,(byte)0xed,(byte)0xe7,
(byte)0x0d,(byte)0x21,(byte)0xc9,(byte)0x06,(byte)0x88,(byte)0x42,(byte)0x02,(byte)0xd2,(byte)0x01,(byte)0x49,(byte)0xc9,(byte)0x6b,(byte)0x08,(byte)0x18,(byte)0x70,(byte)0x47,
(byte)0xc0,(byte)0x1f,(byte)0x00,(byte)0x68,(byte)0xd2,(byte)0xb2,(byte)0x01,(byte)0xe0,(byte)0x02,(byte)0x70,(byte)0x40,(byte)0x1c,(byte)0x49,(byte)0x1e,(byte)0xfb,(byte)0xd2,
(byte)0x70,(byte)0x47,(byte)0x00,(byte)0x22,(byte)0xf6,(byte)0xe7,(byte)0x10,(byte)0xb5,(byte)0x04,(byte)0x46,(byte)0x08,(byte)0x46,(byte)0x11,(byte)0x46,(byte)0x02,(byte)0x46,
(byte)0x20,(byte)0x46,(byte)0xff,(byte)0xf7,(byte)0xef,(byte)0xff,(byte)0x20,(byte)0x46,(byte)0x10,(byte)0xbd,(byte)0x00,(byte)0x00,(byte)0x06,(byte)0x4c,(byte)0x01,(byte)0x25,
(byte)0x06,(byte)0x4e,(byte)0x05,(byte)0xe0,(byte)0x20,(byte)0x46,(byte)0xe3,(byte)0x68,(byte)0x07,(byte)0xc8,(byte)0x2b,(byte)0x43,(byte)0x98,(byte)0x47,(byte)0x10,(byte)0x34,
(byte)0xb4,(byte)0x42,(byte)0xf7,(byte)0xd3,(byte)0xff,(byte)0xf7,(byte)0xd0,(byte)0xfc,(byte)0xd0,(byte)0x06,(byte)0x00,(byte)0x00,(byte)0xf0,(byte)0x06,(byte)0x00,(byte)0x00,
(byte)0x02,(byte)0xe0,(byte)0x08,(byte)0xc8,(byte)0x12,(byte)0x1f,(byte)0x08,(byte)0xc1,(byte)0x00,(byte)0x2a,(byte)0xfa,(byte)0xd1,(byte)0x70,(byte)0x47,(byte)0x70,(byte)0x47,
(byte)0x00,(byte)0x20,(byte)0x01,(byte)0xe0,(byte)0x01,(byte)0xc1,(byte)0x12,(byte)0x1f,(byte)0x00,(byte)0x2a,(byte)0xfb,(byte)0xd1,(byte)0x70,(byte)0x47,(byte)0x00,(byte)0x00,
(byte)0xf0,(byte)0x06,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x04,(byte)0x00,(byte)0x68,(byte)0x00,(byte)0x08,(byte)0x00,(byte)0x00,(byte)0xc0,(byte)0x06,(byte)0x00,(byte)0x00,
(byte)0xf0,(byte)0x06,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x0c,(byte)0x00,(byte)0x68,(byte)0x00,(byte)0x04,(byte)0x00,(byte)0x00,(byte)0xc0,(byte)0x06,(byte)0x00,(byte)0x00
};
}
