/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.microedition.rms.*;

public class PrincipalMidletPTBR extends MIDlet {
    private MenuCanvas canvas;
    public  Display    display;

    public void startApp() {
        if(display==null){
            canvas  = new MenuCanvas(this);
            display = Display.getDisplay(this);
        }
        display.setCurrent(canvas);
    }

    public void pauseApp() {}
    public void destroyApp(boolean unconditional) { }
}

class MenuCanvas extends Canvas implements CommandListener {

    private PrincipalMidletPTBR   pai;
    private byte                  opc       = 0;
    public Command                exitCmd   = new Command("Sair",Command.EXIT,0);
    public Command                insertCmd = new Command("Inserir",Command.OK,1);
    public Command                reportCmd = new Command("Relatório",Command.OK,1);
    public Command                backCmd   = new Command("Voltar",Command.BACK,1);
    public Command                saveCmd   = new Command("Salvar",Command.OK,1);
    private InsertForm            form;
    public String                 regGas;
    public RecordStore            rsGas;

    public MenuCanvas(PrincipalMidletPTBR pai){
        try{
            rsGas = RecordStore.openRecordStore("Gasolina",true);
        }catch(RecordStoreException ex){
            ex.printStackTrace();
        }

        this.pai=pai;
        setCommandListener(this);
        addCommand(exitCmd);
        addCommand(insertCmd);
        addCommand(reportCmd);

    }

    public void paint(Graphics g){
        g.setColor(255,255,255);
        g.fillRect(0,0,getWidth(),getHeight());
        g.setColor(0,64,128);

        switch(opc){

            case 0:
                g.drawString("Menu Principal",0,0,Graphics.TOP | Graphics.LEFT);
                g.setColor(0,114,168);
                g.drawString("Use este aplicativo para controlar",0,24,Graphics.TOP | Graphics.LEFT);
                g.drawString("o consumo de gasolina do seu carro.",0,36,Graphics.TOP | Graphics.LEFT);
                g.drawString("Opções:",0,64,Graphics.TOP | Graphics.LEFT);
                g.drawString("INSERIR - Use para gravar KM/LT",0,76,Graphics.TOP | Graphics.LEFT);
                g.drawString("RELATÓRIO - Lista os 10 últimos consumos",0,88,Graphics.TOP | Graphics.LEFT);
                g.drawString("Marcosptf",0,112,Graphics.TOP | Graphics.LEFT);
                break;

            case 1:
                int    lin=0,od1=0,od2=0;
                double gas=0.0;
                g.drawString("Opção Relatório",0,lin,Graphics.TOP | Graphics.LEFT);
                lin+=14;
                g.drawString("seus últimos 10 consumos foram...",0,lin,Graphics.TOP | Graphics.LEFT);
                lin+=12;
                g.setColor(0,114,168);

                try{
                    if(rsGas.getNumRecords()>1){
                        for(int recId=1;recId<=rsGas.getNumRecords();recId++){
                            regGas = new String(rsGas.getRecord(recId));
                            od1    = Integer.parseInt(regGas.substring(0,regGas.indexOf("|")));
                            gas    = Double.parseDouble(regGas.substring(regGas.indexOf("|")+1));
                            regGas = new String(rsGas.getRecord(recId + 1));
                            od2    = Integer.parseInt(regGas.substring(0,regGas.indexOf("|")));
                            lin    +=15;
                            g.drawString(recId + ") "+ ((od2-od1)/gas)+ "KM/LT",0,lin,Graphics.TOP | Graphics.LEFT);
                        }
                    }
                }catch(RecordStoreException ex){
                    ex.printStackTrace();
                }
                break;
        }
    }

    public void commandAction(Command command,Displayable displayable){
        if(command==exitCmd){
            try{
                rsGas.closeRecordStore();
                pai.destroyApp(true);
                pai.notifyDestroyed();
            }catch(RecordStoreException ex){
                ex.printStackTrace();
            }//catch(MIDletStateChangeException ex){
//                ex.printStackTrace();
//            }
        }else if(command==insertCmd){
            repaint();
            form = new InsertForm("Opção de Inserção");
            pai.display.setCurrent(form);
        }else if(command==reportCmd){
            opc=1;
            repaint();
        }else if(command==backCmd){
            pai.display.setCurrent(this);
            opc=0;
        }else if(command==saveCmd){
            regGas = form.getRegGas();
            byte[] data;

            try{
                if(rsGas.getNumRecords()==11){
                    for(int recId=1;recId<rsGas.getNumRecords();recId++){
                        data = rsGas.getRecord(recId+1);
                        rsGas.setRecord(recId,data,0,data.length);
                    }
                    data = regGas.getBytes();
                    rsGas.setRecord(11,data,0,data.length);
                }else{
                    data = regGas.getBytes();
                    rsGas.addRecord(data,0,data.length);
                }

            }catch(RecordStoreException ex){
                ex.printStackTrace();
            }

            pai.display.setCurrent(this);
            opc=0;
            repaint();
        }

    }

    class InsertForm extends Form {

        private TextField QTDGAS;
        private TextField ODOMETER;

        public InsertForm(String title){
            super(title);
            addCommand(backCmd);
            addCommand(saveCmd);
            setCommandListener(MenuCanvas.this);
            ODOMETER = new TextField("Odometro (KM):","",64,TextField.NUMERIC);
            QTDGAS   = new TextField("Gasolina (LT): ","",64,TextField.DECIMAL);
            this.append(new StringItem("Informe seu Consumo:",""));
            this.append(ODOMETER);
            this.append(QTDGAS);

        }

        public String getRegGas(){
            return ODOMETER.getString()+"|"+QTDGAS.getString();
        }
    }
}

