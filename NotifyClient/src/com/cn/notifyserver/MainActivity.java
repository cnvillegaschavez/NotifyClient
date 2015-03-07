package com.cn.notifyserver;

import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.content.Intent;
import com.cn.notifyserver.Class.*;
import com.cn.notifyserver.BD.DataBaseManager;
import android.database.Cursor;
public class MainActivity extends Activity
{
    /** Called when the activity is first created. */
 	boolean isServerClient=false;
	GeneralCn cgeneral;
    long timeInMilliseconds = 0L;
    MiServicioGps ms;
    private long startTime = 0L;
    private Handler customHandler = new Handler();

    private DataBaseManager manager;
    Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        manager= new DataBaseManager(this);

		if(manager.GetConfigNotify("config"))
		{
		
			if("servidor".equalsIgnoreCase(manager.GetConfigNotifyOption("config"))){
				this.finish();
                Intent serverActivity= new Intent(this, ServerActivity.class);
                startActivity(serverActivity);
            }
            else{

                if(ms==null){
					
                    ms = new MiServicioGps(MainActivity.this.getApplicationContext());
                    cgeneral= new GeneralCn(this);
                    ms.setCoordenadas();
                    startTime = SystemClock.uptimeMillis();
                    customHandler.postDelayed(updateTimerThread, 100);
					
                }else
                    ms.setCoordenadas();

            }

		}
		else{
			
			setContentView(R.layout.main);	
		}
    }
	
	public void OnSaveOption(View view){
		
		if(!isServerClient){

            manager.insertarParameter("config", "cliente");
			ms = new MiServicioGps(MainActivity.this.getApplicationContext());
			cgeneral= new GeneralCn(this);
			ms.setCoordenadas();
			startTime = SystemClock.uptimeMillis();
			customHandler.postDelayed(updateTimerThread, 100);
		}
		else{

            manager.insertarParameter("config", "servidor");
			this.finish();
			//Inicia opción de servidor
    		Intent serverActivity= new Intent(this, ServerActivity.class);
    		startActivity(serverActivity);
		}
	}
	
	public void onRadioGuardarOpcion(View view){
		boolean checked=((RadioButton)view).isChecked();
		switch(view.getId()){
			case R.id.rbtnServidor:
				if(checked)
					isServerClient=true;
					break;
			case R.id.rbtnCliente:
				if(checked)
					isServerClient=false;
				break;
		}
	}
	
    private Runnable updateTimerThread = new Runnable() {
        public void run() {

            new EscucharMensaje().execute();
            timeInMilliseconds = SystemClock.uptimeMillis() - startTime;
            customHandler.postDelayed(this, 3000);
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_client, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.item) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class EscucharMensaje extends AsyncTask<Void, Void, Void>{

        @Override
        protected void onPreExecute(){
            Toast.makeText(getApplicationContext(), "Buscando ultima posición ...", 
			Toast.LENGTH_SHORT).show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            try {

                cgeneral.readSMS();
                ms.setCoordenadas();
                if(cgeneral.messageBody.trim().equalsIgnoreCase("."))
                    cgeneral.sendSMS(cgeneral.phoneNumber, ms.messageBody);
                timeInMilliseconds = SystemClock.uptimeMillis() - startTime;

            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid){
            Toast.makeText(getApplicationContext(), "Resultado - Nº: "+cgeneral.phoneNumber +" - Posición: "+ms.messageBody, Toast.LENGTH_SHORT).show();
        }
    }
}
