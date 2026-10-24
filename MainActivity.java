package com.formmaster.app;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.Gravity;
import android.widget.*;
import java.io.*;
import java.util.ArrayList;
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.multipdf.PDFMergerUtility;
import com.tom_roush.pdfbox.io.MemoryUsageSetting;

public class MainActivity extends Activity {
    static final int PICK=1, MULTI=2, IMAGE=3, SAVE=4;
    LinearLayout root;
    boolean hi=true;
    String task="";
    ArrayList<Uri> pdfs=new ArrayList<>();
    Uri imageUri;
    int start=1,end=1,w=600,h=800,q=85;
    String tr(String a,String b){return hi?a+" / "+b:b;}
    int dp(int n){return (int)(n*getResources().getDisplayMetrics().density);}
    @Override public void onCreate(Bundle b){super.onCreate(b);PDFBoxResourceLoader.init(getApplicationContext());ui();}
    void ui(){
        ScrollView sc=new ScrollView(this);root=new LinearLayout(this);root.setOrientation(1);root.setPadding(dp(18),dp(16),dp(18),dp(24));root.setBackgroundColor(Color.rgb(249,250,252));sc.addView(root);setContentView(sc);
        TextView title=new TextView(this);title.setText("Form Master");title.setTextSize(29);title.setTextColor(Color.rgb(185,40,35));title.setTypeface(null,1);title.setGravity(Gravity.CENTER);root.addView(title);
        TextView sub=new TextView(this);sub.setText(tr("PDF, फोटो और फॉर्म टूल्स","PDF, photo and form tools"));sub.setGravity(Gravity.CENTER);root.addView(sub);
        button("हिंदी / English",v->{hi=!hi;ui();});
        heading(tr("PDF टूल्स","PDF tools"));
        button(tr("PDF जोड़ें (Merge)","Merge PDFs"),v->pickMulti());
        button(tr("PDF पेज निकालें (Split)","Split PDF pages"),v->splitDialog());
        button(tr("PDF छोटा करें (Compress)","Compress PDF"),v->pickPdf("compress"));
        heading(tr("फोटो और हस्ताक्षर","Photo & signature"));
        button(tr("फोटो का KB/साइज बदलें","Resize photo / signature"),v->pickImage());
        heading(tr("फॉर्म चेकलिस्ट","Form checklist"));
        button(tr("जरूरी दस्तावेजों की सूची","Document checklist"),v->checklist());
        TextView note=new TextView(this);note.setText(tr("फाइलें फोन पर प्रोसेस होती हैं। संवेदनशील दस्तावेजों की कॉपी सुरक्षित रखें।","Files are processed on your phone. Keep copies of sensitive documents safe."));note.setPadding(0,dp(16),0,0);root.addView(note);
    }
    void heading(String s){TextView t=new TextView(this);t.setText(s);t.setTextSize(19);t.setTypeface(null,1);t.setTextColor(Color.rgb(40,55,75));t.setPadding(0,dp(18),0,dp(5));root.addView(t);}
    void button(String s,android.view.View.OnClickListener l){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setOnClickListener(l);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=dp(6);root.addView(b,p);}
    void pickPdf(String a){task=a;Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("application/pdf");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,PICK);}
    void pickMulti(){task="merge";Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("application/pdf");i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE,true);i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,MULTI);}
    void pickImage(){task="image";Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("image/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,IMAGE);}
    void splitDialog(){
        EditText a=new EditText(this);a.setText("1");a.setHint("Start page");a.setInputType(2);
        EditText b=new EditText(this);b.setText("1");b.setHint("End page");b.setInputType(2);
        LinearLayout box=new LinearLayout(this);box.setOrientation(1);box.setPadding(dp(18),0,dp(18),0);box.addView(a);box.addView(b);
        new android.app.AlertDialog.Builder(this).setTitle(tr("पेज रेंज चुनें","Choose page range")).setView(box).setPositiveButton(tr("PDF चुनें","Choose PDF"),(d,x)->{try{start=Integer.parseInt(a.getText().toString());end=Integer.parseInt(b.getText().toString());if(start<1||end<start)throw new Exception();pickPdf("split");}catch(Exception e){msg("Invalid page range");}}).setNegativeButton("Cancel",null).show();
    }
    void checklist(){String s=tr("सामान्य चेकलिस्ट\\n\\n☐ पहचान पत्र (यदि जरूरी हो)\\n☐ पता प्रमाण (यदि जरूरी हो)\\n☐ फोटो और हस्ताक्षर\\n☐ जन्मतिथि/शैक्षणिक प्रमाणपत्र (फॉर्म के अनुसार)\\n☐ मोबाइल नंबर और ईमेल\\n☐ फीस रसीद (यदि लागू हो)\\n\\nहर फॉर्म की आधिकारिक सूचना देखें।","General checklist\\n\\n☐ ID proof if required\\n☐ Address proof if required\\n☐ Photo and signature\\n☐ DOB/education certificate as required\\n☐ Mobile and email\\n☐ Fee receipt if applicable\\n\\nAlways check the official form notice.");new android.app.AlertDialog.Builder(this).setTitle(tr("दस्तावेज़ चेकलिस्ट","Document checklist")).setMessage(s).setPositiveButton("OK",null).show();}
    @Override protected void onActivityResult(int r,int c,Intent d){super.onActivityResult(r,c,d);if(c!=RESULT_OK||d==null)return;
        if(r==PICK){Uri u=d.getData();if(u!=null){pdfs.clear();pdfs.add(u);savePicker(task,task.equals("split")?"split-pages.pdf":task.equals("compress")?"compressed.pdf":"output.pdf","application/pdf");}}
        else if(r==MULTI){pdfs.clear();if(d.getClipData()!=null){for(int i=0;i<d.getClipData().getItemCount();i++)pdfs.add(d.getClipData().getItemAt(i).getUri());}else if(d.getData()!=null)pdfs.add(d.getData());if(pdfs.size()<2){msg(tr("कम से कम 2 PDF चुनें","Select at least 2 PDFs"));return;}savePicker("merge","merged.pdf","application/pdf");}
        else if(r==IMAGE){imageUri=d.getData();if(imageUri!=null)imageDialog();}
        else if(r==SAVE){Uri out=d.getData();if(out!=null){final String a=task;new Thread(()->{try{if(a.equals("image"))resize(out);else if(a.equals("merge"))merge(out);else if(a.equals("split"))split(out);else if(a.equals("compress"))compress(out);runOnUiThread(()->msg(tr("काम पूरा हुआ।","Done.")));}catch(Exception e){runOnUiThread(()->msg(tr("त्रुटि: ","Error: ")+e.getMessage()));}}).start();}}
    }
    void savePicker(String a,String name,String mime){task=a;Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType(mime);i.putExtra(Intent.EXTRA_TITLE,name);startActivityForResult(i,SAVE);}
    File cacheCopy(Uri u)throws Exception{File f=File.createTempFile("fm-in-",".pdf",getCacheDir());try(InputStream in=getContentResolver().openInputStream(u);OutputStream o=new FileOutputStream(f)){if(in==null)throw new IOException("Cannot open file");byte[] b=new byte[8192];int n;while((n=in.read(b))>0)o.write(b,0,n);}return f;}
    void write(File f,Uri u)throws Exception{try(InputStream in=new FileInputStream(f);OutputStream o=getContentResolver().openOutputStream(u,"w")){if(o==null)throw new IOException("Cannot save");byte[] b=new byte[8192];int n;while((n=in.read(b))>0)o.write(b,0,n);}}
    void merge(Uri out)throws Exception{File tmp=File.createTempFile("fm-merge-",".pdf",getCacheDir());PDFMergerUtility m=new PDFMergerUtility();for(Uri u:pdfs)m.addSource(cacheCopy(u));m.setDestinationFileName(tmp.getAbsolutePath());m.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());write(tmp,out);}
    void split(Uri out)throws Exception{File src=cacheCopy(pdfs.get(0));try(PDDocument in=PDDocument.load(src);PDDocument result=new PDDocument()){if(end>in.getNumberOfPages())throw new IOException("PDF has "+in.getNumberOfPages()+" pages");for(int i=start-1;i<end;i++)result.importPage(in.getPage(i));File tmp=File.createTempFile("fm-split-",".pdf",getCacheDir());result.save(tmp);write(tmp,out);}}
    void compress(Uri out)throws Exception{File src=cacheCopy(pdfs.get(0));try(PDDocument d=PDDocument.load(src)){File tmp=File.createTempFile("fm-compress-",".pdf",getCacheDir());d.save(tmp);write(tmp,out);}}
    void imageDialog(){EditText a=new EditText(this);a.setText("600");a.setHint("Width px");a.setInputType(2);EditText b=new EditText(this);b.setText("800");b.setHint("Height px");b.setInputType(2);EditText c=new EditText(this);c.setText("85");c.setHint("JPEG quality 1-100");c.setInputType(2);LinearLayout box=new LinearLayout(this);box.setOrientation(1);box.setPadding(dp(18),0,dp(18),0);box.addView(a);box.addView(b);box.addView(c);new android.app.AlertDialog.Builder(this).setTitle(tr("फोटो साइज","Photo size")).setView(box).setPositiveButton(tr("सेव","Save"),(d,x)->{try{w=Integer.parseInt(a.getText().toString());h=Integer.parseInt(b.getText().toString());q=Integer.parseInt(c.getText().toString());if(w<1||h<1||w>5000||h>5000||q<1||q>100)throw new Exception();savePicker("image","resized-photo.jpg","image/jpeg");}catch(Exception e){msg("Invalid values");}}).setNegativeButton("Cancel",null).show();}
    void resize(Uri out)throws Exception{Bitmap src;try(InputStream in=getContentResolver().openInputStream(imageUri)){src=BitmapFactory.decodeStream(in);}if(src==null)throw new IOException("Unsupported image");Bitmap dst=Bitmap.createScaledBitmap(src,w,h,true);try(OutputStream o=getContentResolver().openOutputStream(out)){if(o==null)throw new IOException("Cannot save");dst.compress(Bitmap.CompressFormat.JPEG,q,o);}if(dst!=src)dst.recycle();src.recycle();}
    void msg(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
}
