/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /Users/mvigil/ODK/workspace_devl/StubApp/src/org/opendatakit/submit/scheduling/ClientRemote.aidl
 */
package org.opendatakit.submit.scheduling;
public interface ClientRemote extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements org.opendatakit.submit.scheduling.ClientRemote
{
private static final java.lang.String DESCRIPTOR = "org.opendatakit.submit.scheduling.ClientRemote";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an org.opendatakit.submit.scheduling.ClientRemote interface,
 * generating a proxy if needed.
 */
public static org.opendatakit.submit.scheduling.ClientRemote asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof org.opendatakit.submit.scheduling.ClientRemote))) {
return ((org.opendatakit.submit.scheduling.ClientRemote)iin);
}
return new org.opendatakit.submit.scheduling.ClientRemote.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
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
case TRANSACTION_send:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
java.lang.String _result = this.send(_arg0, _arg1, _arg2);
reply.writeNoException();
reply.writeString(_result);
return true;
}
case TRANSACTION_create:
{
data.enforceInterface(DESCRIPTOR);
org.opendatakit.submit.flags.SyncType _arg0;
if ((0!=data.readInt())) {
_arg0 = org.opendatakit.submit.flags.SyncType.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
java.lang.String _arg3;
_arg3 = data.readString();
java.lang.String _result = this.create(_arg0, _arg1, _arg2, _arg3);
reply.writeNoException();
reply.writeString(_result);
if ((_arg0!=null)) {
reply.writeInt(1);
_arg0.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_download:
{
data.enforceInterface(DESCRIPTOR);
org.opendatakit.submit.flags.SyncType _arg0;
if ((0!=data.readInt())) {
_arg0 = org.opendatakit.submit.flags.SyncType.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
java.lang.String _arg3;
_arg3 = data.readString();
java.lang.String _result = this.download(_arg0, _arg1, _arg2, _arg3);
reply.writeNoException();
reply.writeString(_result);
if ((_arg0!=null)) {
reply.writeInt(1);
_arg0.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_sync:
{
data.enforceInterface(DESCRIPTOR);
org.opendatakit.submit.flags.SyncType _arg0;
if ((0!=data.readInt())) {
_arg0 = org.opendatakit.submit.flags.SyncType.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
java.lang.String _arg3;
_arg3 = data.readString();
java.lang.String _result = this.sync(_arg0, _arg1, _arg2, _arg3);
reply.writeNoException();
reply.writeString(_result);
if ((_arg0!=null)) {
reply.writeInt(1);
_arg0.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_delete:
{
data.enforceInterface(DESCRIPTOR);
org.opendatakit.submit.flags.SyncType _arg0;
if ((0!=data.readInt())) {
_arg0 = org.opendatakit.submit.flags.SyncType.CREATOR.createFromParcel(data);
}
else {
_arg0 = null;
}
java.lang.String _arg1;
_arg1 = data.readString();
java.lang.String _arg2;
_arg2 = data.readString();
java.lang.String _arg3;
_arg3 = data.readString();
java.lang.String _result = this.delete(_arg0, _arg1, _arg2, _arg3);
reply.writeNoException();
reply.writeString(_result);
if ((_arg0!=null)) {
reply.writeInt(1);
_arg0.writeToParcel(reply, android.os.Parcelable.PARCELABLE_WRITE_RETURN_VALUE);
}
else {
reply.writeInt(0);
}
return true;
}
case TRANSACTION_onQueue:
{
data.enforceInterface(DESCRIPTOR);
java.lang.String _arg0;
_arg0 = data.readString();
boolean _result = this.onQueue(_arg0);
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
case TRANSACTION_queueSize:
{
data.enforceInterface(DESCRIPTOR);
int _result = this.queueSize();
reply.writeNoException();
reply.writeInt(_result);
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements org.opendatakit.submit.scheduling.ClientRemote
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public java.lang.String send(java.lang.String uri, java.lang.String pathname, java.lang.String uid) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(uri);
_data.writeString(pathname);
_data.writeString(uid);
mRemote.transact(Stub.TRANSACTION_send, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String create(org.opendatakit.submit.flags.SyncType st, java.lang.String uri, java.lang.String pathname, java.lang.String uid) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((st!=null)) {
_data.writeInt(1);
st.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
_data.writeString(uri);
_data.writeString(pathname);
_data.writeString(uid);
mRemote.transact(Stub.TRANSACTION_create, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
if ((0!=_reply.readInt())) {
st.readFromParcel(_reply);
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String download(org.opendatakit.submit.flags.SyncType st, java.lang.String uri, java.lang.String pathname, java.lang.String uid) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((st!=null)) {
_data.writeInt(1);
st.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
_data.writeString(uri);
_data.writeString(pathname);
_data.writeString(uid);
mRemote.transact(Stub.TRANSACTION_download, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
if ((0!=_reply.readInt())) {
st.readFromParcel(_reply);
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String sync(org.opendatakit.submit.flags.SyncType st, java.lang.String uri, java.lang.String pathname, java.lang.String uid) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((st!=null)) {
_data.writeInt(1);
st.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
_data.writeString(uri);
_data.writeString(pathname);
_data.writeString(uid);
mRemote.transact(Stub.TRANSACTION_sync, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
if ((0!=_reply.readInt())) {
st.readFromParcel(_reply);
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public java.lang.String delete(org.opendatakit.submit.flags.SyncType st, java.lang.String uri, java.lang.String pathname, java.lang.String uid) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
java.lang.String _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
if ((st!=null)) {
_data.writeInt(1);
st.writeToParcel(_data, 0);
}
else {
_data.writeInt(0);
}
_data.writeString(uri);
_data.writeString(pathname);
_data.writeString(uid);
mRemote.transact(Stub.TRANSACTION_delete, _data, _reply, 0);
_reply.readException();
_result = _reply.readString();
if ((0!=_reply.readInt())) {
st.readFromParcel(_reply);
}
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public boolean onQueue(java.lang.String uid) throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
_data.writeString(uid);
mRemote.transact(Stub.TRANSACTION_onQueue, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
@Override public int queueSize() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
int _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_queueSize, _data, _reply, 0);
_reply.readException();
_result = _reply.readInt();
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_send = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
static final int TRANSACTION_create = (android.os.IBinder.FIRST_CALL_TRANSACTION + 1);
static final int TRANSACTION_download = (android.os.IBinder.FIRST_CALL_TRANSACTION + 2);
static final int TRANSACTION_sync = (android.os.IBinder.FIRST_CALL_TRANSACTION + 3);
static final int TRANSACTION_delete = (android.os.IBinder.FIRST_CALL_TRANSACTION + 4);
static final int TRANSACTION_onQueue = (android.os.IBinder.FIRST_CALL_TRANSACTION + 5);
static final int TRANSACTION_queueSize = (android.os.IBinder.FIRST_CALL_TRANSACTION + 6);
}
public java.lang.String send(java.lang.String uri, java.lang.String pathname, java.lang.String uid) throws android.os.RemoteException;
public java.lang.String create(org.opendatakit.submit.flags.SyncType st, java.lang.String uri, java.lang.String pathname, java.lang.String uid) throws android.os.RemoteException;
public java.lang.String download(org.opendatakit.submit.flags.SyncType st, java.lang.String uri, java.lang.String pathname, java.lang.String uid) throws android.os.RemoteException;
public java.lang.String sync(org.opendatakit.submit.flags.SyncType st, java.lang.String uri, java.lang.String pathname, java.lang.String uid) throws android.os.RemoteException;
public java.lang.String delete(org.opendatakit.submit.flags.SyncType st, java.lang.String uri, java.lang.String pathname, java.lang.String uid) throws android.os.RemoteException;
public boolean onQueue(java.lang.String uid) throws android.os.RemoteException;
public int queueSize() throws android.os.RemoteException;
}
