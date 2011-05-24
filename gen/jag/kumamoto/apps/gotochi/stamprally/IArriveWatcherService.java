/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: E:\\android\\work\\jagkuma-aharisu\\jagkuma\\src\\jag\\kumamoto\\apps\\gotochi\\stamprally\\IArriveWatcherService.aidl
 */
package jag.kumamoto.apps.gotochi.stamprally;
public interface IArriveWatcherService extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements jag.kumamoto.apps.gotochi.stamprally.IArriveWatcherService
{
private static final java.lang.String DESCRIPTOR = "jag.kumamoto.apps.gotochi.stamprally.IArriveWatcherService";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an jag.kumamoto.apps.gotochi.stamprally.IArriveWatcherService interface,
 * generating a proxy if needed.
 */
public static jag.kumamoto.apps.gotochi.stamprally.IArriveWatcherService asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = (android.os.IInterface)obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof jag.kumamoto.apps.gotochi.stamprally.IArriveWatcherService))) {
return ((jag.kumamoto.apps.gotochi.stamprally.IArriveWatcherService)iin);
}
return new jag.kumamoto.apps.gotochi.stamprally.IArriveWatcherService.Stub.Proxy(obj);
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
case TRANSACTION_resetupLocationListener:
{
data.enforceInterface(DESCRIPTOR);
this.resetupLocationListener();
reply.writeNoException();
return true;
}
case TRANSACTION_showArriveNotification:
{
data.enforceInterface(DESCRIPTOR);
jag.kumamoto.apps.gotochi.stamprally.Data.StampPin _arg0;
if ((0!=data.readInt())) {
_arg0 = jag.kumamoto.apps.gotochi.stamprally.Data.StampPin.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.showArriveNotification(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_removeArriveNotification:
{
data.enforceInterface(DESCRIPTOR);
long _arg0;
_arg0 = data.readLong();
this.removeArriveNotification(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_getArrivedStampPins:
{
data.enforceInterface(DESCRIPTOR);
long[] _result = this.getArrivedStampPins();
reply.writeNoException();
reply.writeLongArray(_result);
return true;
}
case TRANSACTION_changeArriveCheckInterval:
{
data.enforceInterface(DESCRIPTOR);
int _arg0;
_arg0 = data.readInt();
this.changeArriveCheckInterval(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_checkArrive:
{
data.enforceInterface(DESCRIPTOR);
this.checkArrive();
reply.writeNoException();
return true;
}
case TRANSACTION_onLocationChanged:
{
data.enforceInterface(DESCRIPTOR);
android.location.Location _arg0;
if ((0!=data.readInt())) {
_arg0 = android.location.Location.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
this.onLocationChanged(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_registerApproachCallback:
{
data.enforceInterface(DESCRIPTOR);
jag.kumamoto.apps.gotochi.stamprally.IApproachPinCallback _arg0;
_arg0 = jag.kumamoto.apps.gotochi.stamprally.IApproachPinCallback.Stub.asInterface(data.readStrongBinder());
this.registerApproachCallback(_arg0);
reply.writeNoException();
return true;
}
case TRANSACTION_unregisterApproachCallback:
{
data.enforceInterface(DESCRIPTOR);
jag.kumamoto.apps.gotochi.stamprally.IApproachPinCallback _arg0;
_arg0 = jag.kumamoto.apps.gotochi.stamprally.IApproachPinCallback.Stub.asInterface(data.readStrongBinder());
this.unregisterApproachCallback(_arg0);
reply.writeNoException();
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements jag.kumamoto.apps.gotochi.stamprally.IArriveWatcherService
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
public void resetupLocationListener() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_resetupLocationListener, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void showArriveNotification(jag.kumamoto.apps.gotochi.stamprally.Data.StampPin pin) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((pin!=null)) {
_data.writeInt(1);
pin.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_showArriveNotification, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void removeArriveNotification(long pinId) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeLong(pinId);
mRemote.transact(Stub.TRANSACTION_removeArriveNotification, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public long[] getArrivedStampPins() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
long[] _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_getArrivedStampPins, _data, _reply, 0);
_reply.readException();
_result = _reply.createLongArray();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
public void changeArriveCheckInterval(int type) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeInt(type);
mRemote.transact(Stub.TRANSACTION_changeArriveCheckInterval, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void checkArrive() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_checkArrive, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void onLocationChanged(android.location.Location location) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((location!=null)) {
_data.writeInt(1);
location.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
mRemote.transact(Stub.TRANSACTION_onLocationChanged, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void registerApproachCallback(jag.kumamoto.apps.gotochi.stamprally.IApproachPinCallback callback) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((callback!=null))?(callback.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_registerApproachCallback, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
public void unregisterApproachCallback(jag.kumamoto.apps.gotochi.stamprally.IApproachPinCallback callback) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeStrongBinder((((callback!=null))?(callback.asBinder()):(null)));
mRemote.transact(Stub.TRANSACTION_unregisterApproachCallback, _data, _reply, 0);
_reply.readException();
}
finally {
_reply.recycle();
_data.recycle();
}
}
}
static final int TRANSACTION_resetupLocationListener = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_showArriveNotification = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_removeArriveNotification = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_getArrivedStampPins = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_changeArriveCheckInterval = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_checkArrive = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_onLocationChanged = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
static final int TRANSACTION_registerApproachCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 7);
static final int TRANSACTION_unregisterApproachCallback = (android.os.IBinder.FIRST_CALL_TRANSACTION + 8);
}
public void resetupLocationListener() throws android.os.RemoteException;
public void showArriveNotification(jag.kumamoto.apps.gotochi.stamprally.Data.StampPin pin) throws android.os.RemoteException;
public void removeArriveNotification(long pinId) throws android.os.RemoteException;
public long[] getArrivedStampPins() throws android.os.RemoteException;
public void changeArriveCheckInterval(int type) throws android.os.RemoteException;
public void checkArrive() throws android.os.RemoteException;
public void onLocationChanged(android.location.Location location) throws android.os.RemoteException;
public void registerApproachCallback(jag.kumamoto.apps.gotochi.stamprally.IApproachPinCallback callback) throws android.os.RemoteException;
public void unregisterApproachCallback(jag.kumamoto.apps.gotochi.stamprally.IApproachPinCallback callback) throws android.os.RemoteException;
}
