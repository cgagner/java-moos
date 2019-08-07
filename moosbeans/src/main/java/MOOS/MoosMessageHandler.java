/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package MOOS;

import java.util.ArrayList;

/**
 *
 * @author cgagner
 */
public interface MoosMessageHandler {

    public boolean handleMessages(ArrayList<MOOSMsg> messages);

}
