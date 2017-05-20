package com.diegeilstegruppe.sasha.service;

import com.squareup.otto.Bus;

/**
 * Created by denys on 20/05/2017.
 */

public class BusProvider {

    private static final Bus BUS = new Bus();

    public static Bus getInstance(){
        return BUS;
    }

    public BusProvider(){}
}
