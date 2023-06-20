/*
 * Copyright (C) 2019-2022 Federico Dossena
 *               2019 The MoKee Open Source Project
 *               2023 someone5678
 * SPDX-License-Identifier: GPL-3.0-or-later
 * License-Filename: LICENSE
 */

package com.android.bluetooth.bthelper;

import android.bluetooth.BluetoothA2dp;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.ParcelUuid;
import android.os.UserHandle;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import com.android.bluetooth.bthelper.pods.PodsService;

import java.util.Objects;

public class StartupReceiver extends BroadcastReceiver {

    public static final Set<ParcelUuid> PodsUUIDS = new HashSet<>();
    static {
        PodsUUIDS.add(ParcelUuid.fromString("74ec2172-0bad-4d01-8f77-997b2be0722a"));
        PodsUUIDS.add(ParcelUuid.fromString("2a72e02b-7b99-778f-014d-ad0b7221ec74"));
    }

    @Override
    public void onReceive (Context context, Intent intent) {
        if (intent == null) return;
        if (BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED.equals(Objects.requireNonNull(intent.getAction()))) {
            final int state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothAdapter.ERROR);
            final BluetoothDevice device = Objects.requireNonNull(intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
            btProfileChanges(Objects.requireNonNull(context), state, device);
        }
    }

    public static boolean isPods (BluetoothDevice device) {
        for (ParcelUuid uuid : device.getUuids()) {
            if (PodsUUIDS.contains(uuid)) {
                return true;
            }
        }
        return false;
    }

    private void startPodsService (Context context, BluetoothDevice device) {
        if (!isPods(device)) {
            return;
        }
        final Intent intent = new Intent(context, PodsService.class);
        intent.putExtra(BluetoothDevice.EXTRA_DEVICE, device);
        context.startServiceAsUser(intent, UserHandle.CURRENT);
    }

    private void stopPodsService (Context context) {
        context.stopServiceAsUser(new Intent(context, PodsService.class),
                                    UserHandle.CURRENT);
    }

    private void btProfileChanges (Context context, int state, BluetoothDevice device) {
        switch (state) {
            case BluetoothProfile.STATE_CONNECTED:
                startPodsService(context, device);
                break;
            case BluetoothProfile.STATE_DISCONNECTING:
            case BluetoothProfile.STATE_DISCONNECTED:
                stopPodsService(context);
                break;
        }
    }
}
