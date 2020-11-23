package descomplica.desenvolvimentomobile.aula13_4;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcA;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    IntentFilter[] intentFilterEscrita;
    Tag tag;
    EditText etMensagem;
    Button btnSalvar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etMensagem = findViewById(R.id.etMensagem);
        btnSalvar = findViewById(R.id.btnSalvar);
        btnSalvar.setOnClickListener(view -> {
            try {
                if(tag == null) {
                    Toast.makeText(getApplicationContext(),
                            "Nenhuma etiqueta NFC encontrada", Toast.LENGTH_LONG).show();
                } else {
                    gravarNdef(etMensagem.getText().toString(), tag);
                }
            } catch (IOException exception) {
                Toast.makeText(getApplicationContext(),
                        "Erro ao gravar etiqueta", Toast.LENGTH_LONG).show();
                exception.printStackTrace();
            } catch (FormatException exception) {
                Toast.makeText(getApplicationContext(),
                        "Erro ao gravar etiqueta", Toast.LENGTH_LONG).show();
                exception.printStackTrace();
            }
        });

        nfcAdapter = NfcAdapter.getDefaultAdapter(getApplicationContext());

        if (nfcAdapter == null) {
            Toast.makeText(getApplicationContext(),
                    "Dispositivo não possui NFC", Toast.LENGTH_LONG).show();
            finish();
        }

        lerIntent(getIntent());
        Intent intent = new Intent(getApplicationContext(), getClass());
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                0, intent, 0);

        IntentFilter intentFilter = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        intentFilterEscrita = new IntentFilter[] { intentFilter };
    }

    private void lerIntent(Intent intent) {
        String action = intent.getAction();

        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
        || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
        || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] mensagens = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] mensagensNdef = null;

            if (mensagens != null) {
                mensagensNdef = new NdefMessage[mensagens.length];
                for (int i = 0; i < mensagensNdef.length; i++) {
                    mensagensNdef[i] = (NdefMessage) mensagens[i];
                }
                renderizarMensagem(mensagensNdef);
            }
        }
    }

    private void renderizarMensagem(NdefMessage[] mensagensNdef) {
        if (mensagensNdef == null || mensagensNdef.length == 0) {
            return;
        }

        String mensagem;

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getApplicationContext());

        byte[] payload = mensagensNdef[0].getRecords()[0].getPayload();
        mensagem = new String(payload);

        alertDialogBuilder.setTitle("NFC lido!")
                .setMessage(mensagem)
                .setPositiveButton("Ok", (dialogInterface, i) -> {

                }).show();
    }

    private void gravarNdef(String mensagem, Tag tag) throws IOException, FormatException {
        NdefRecord[] ndefRecords = { new NdefRecord(NdefRecord.TNF_WELL_KNOWN,
                NdefRecord.RTD_TEXT, new byte[0], mensagem.getBytes()) };
        NdefMessage ndefMessage = new NdefMessage(ndefRecords);
        Ndef ndef = Ndef.get(tag);
        ndef.connect();
        ndef.writeNdefMessage(ndefMessage);
        ndef.close();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        lerIntent(intent);

        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
            tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFilterEscrita, null);
    }
}