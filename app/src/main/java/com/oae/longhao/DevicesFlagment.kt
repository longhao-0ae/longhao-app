package com.hoho.android.usbserial.examples

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Bundle
import androidx.fragment.app.ListFragment
import com.hoho.android.usbserial.driver.UsbSerialDriver
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.oae.longhao.CustomProber
import java.util.*

//listitemsにデータが入るぽい？
class DevicesFragment : ListFragment() {
    internal class ListItem(var device: UsbDevice, var port: Int, var driver: UsbSerialDriver?)

    private val listItems = ArrayList<ListItem>()
    private var baudRate = 19200
    private var withIoManager = true
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
       /* listAdapter = object : ArrayAdapter<ListItem?>(
            activity!!, 0, listItems
        ) {
            override fun getView(position: Int, view: View?, parent: ViewGroup): View {
                var view = view
                val item = listItems[position]
                if (view == null) view =
                    activity!!.layoutInflater.inflate(R.layout.device_list_item, parent, false)
                val text1 = view!!.findViewById<TextView>(R.id.text1)
                val text2 = view.findViewById<TextView>(R.id.text2)
                if (item.driver == null) text1.text =
                    "<no driver>" else if (item.driver!!.ports.size == 1) text1.text =
                    item.driver!!.javaClass.simpleName.replace("SerialDriver", "") else text1.text =
                    item.driver!!.javaClass.simpleName.replace(
                        "SerialDriver",
                        ""
                    ) + ", Port " + item.port
                text2.text = String.format(
                    Locale.US,
                    "Vendor %04X, Product %04X",
                    item.device.vendorId,
                    item.device.productId
                )
                return view
            }
        }*/
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
     //   val header: View =
     //       activity!!.layoutInflater.inflate(R.layout.device_list_header, null, false)
       // listView.addHeaderView(header, null, false)
       // setEmptyText("<no USB devices found>")
      //  (listView.emptyView as TextView).textSize = 18f
    }
/*
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_devices, menu)
    }*/

    override fun onResume() {
        super.onResume()
        refresh()
    }

   /* override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        return if (id == R.id.refresh) {
            refresh()
            true
        } else if (id == R.id.baud_rate) {
            val values = resources.getStringArray(R.array.baud_rates)
            val pos = Arrays.asList(*values).indexOf(baudRate.toString())
            val builder = AlertDialog.Builder(
                activity
            )
            builder.setTitle("Baud rate")
            builder.setSingleChoiceItems(values, pos) { dialog: DialogInterface, which: Int ->
                baudRate = values[which].toInt()
                dialog.dismiss()
            }
            builder.create().show()
            true
        } else if (id == R.id.read_mode) {
            val values = resources.getStringArray(R.array.read_modes)
            val pos =
                if (withIoManager) 0 else 1 // read_modes[0]=event/io-manager, read_modes[1]=direct
            val builder = AlertDialog.Builder(
                activity
            )
            builder.setTitle("Read mode")
            builder.setSingleChoiceItems(values, pos) { dialog: DialogInterface, which: Int ->
                withIoManager = which == 0
                dialog.dismiss()
            }
            builder.create().show()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }
    */

    fun refresh() {
        val usbManager = activity!!.getSystemService(Context.USB_SERVICE) as UsbManager
        val usbDefaultProber = UsbSerialProber.getDefaultProber()
        val usbCustomProber = CustomProber.getCustomProber()
        listItems.clear()
        for (device in usbManager.deviceList.values) {
            var driver = usbDefaultProber.probeDevice(device)
            if (driver == null) {
                driver = usbCustomProber.probeDevice(device)
            }
            if (driver != null) {
                for (port in driver.ports.indices) listItems.add(
                    ListItem(
                        device!!, port, driver
                    )
                )
            } else {
                listItems.add(ListItem(device!!, 0, null))
            }
        }
     //   listAdapter!!.notifyDataSetChanged()
    }

   /* override fun onListItemClick(l: ListView, v: View, position: Int, id: Long) {
        val item = listItems[position - 1]
        if (item.driver == null) {
            Toast.makeText(activity, "no driver", Toast.LENGTH_SHORT).show()
        } else {
            val args = Bundle()
            args.putInt("device", item.device.deviceId)
            args.putInt("port", item.port)
            args.putInt("baud", baudRate)
            args.putBoolean("withIoManager", withIoManager)
            val fragment: Fragment = TerminalFragment()
            fragment.arguments = args
            fragmentManager!!.beginTransaction().replace(R.id.fragment, fragment, "terminal")
                .addToBackStack(null).commit()
        }
    }*/
}
