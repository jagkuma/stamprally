/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: E:\\android\\work\\jagkuma-aharisu\\jagkuma\\src\\jag\\kumamoto\\apps\\StampRally\\IApproachPinCallback.aidl
 */
package jag.kumamoto.apps.StampRally;
public interface IApproachPinCallback extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements jag.kumamoto.apps.StampRally.IApproachPinCallback
{
private static final java.lang.String DESCRIPTOR = "jag.kumamoto.apps.StampRally.IApproachPinCallback";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an jag.kumamoto.apps.StampRally.IApproachPinCallback interface,
 * generating a proxy if needed.
 */
public static jag.kumamoto.apps.StampRally.IApproachPinCallback asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof jag.kumamoto.apps.StampRally.IApproachPinCallback))) {
return ((jag.kumamoto.apps.StampRally.IApproachPinCallback)iin);
}
return new jag.kumamoto.apps.StampRally.IApproachPinCallback.Stub.Proxy(obj);
}
public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_onApproachPin:
{
data.enforceInterface(DESCRIPTOR);
jag.kumamoto.apps.StampRally.Data.StampPin _arg0;
if ((0!=data.readInt())) {
_arg0 = jag.kumamoto.apps.StampRally.Data.StampPin.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
int _arg1;
_arg1 = data.readInt();
this.onApproachPin(_arg0, _arg1);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements jag.kumamoto.apps.StampRally.IApproachPinCallback
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
public void onApproachPin(jag.kumamoto.apps.StampRally.Data.StampPin pin, int distanceInMeter) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((pin!=null)) {
_data.writeInt(1);
pin.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
_data.writeInt(distanceInMeter);
mRemote.transact(Stub.TRANSACTION_onApproachPin, _data, null, android.os.IBinder.FLAG_ONEWAY);
}
finally {
_data.recycle();
}
}
}
static final int TRANSACTION_onApproachPin = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public void onApproachPin(jag.kumamoto.apps.StampRally.Data.StampPin pin, int distanceInMeter) throws android.os.RemoteException;
}
