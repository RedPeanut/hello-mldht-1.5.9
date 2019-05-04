package hello.swt.osx;

import java.lang.reflect.*;

import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.C;

import hello.util.Log;

public class CocoaUIEnhancer {

	private static String TAG = CocoaUIEnhancer.class.getSimpleName();
			
	private static Object /*Callback*/ callBack3;
	private static long callBack3Addr;
	private static Object /*Callback*/ callBack4;
	private static long callBack4Addr;
	
	/*static final byte[] SWT_OBJECT = {'S','W','T','_','O','B','J','E','C','T','\0'};
	
	private static Class<?> osCls = classForName("org.eclipse.swt.internal.cocoa.OS");
	private static Class<?> nsidCls = classForName("org.eclipse.swt.internal.cocoa.id");*/
	
	/*static {
		
		Class<CocoaUIEnhancer> clazz = CocoaUIEnhancer.class;
		Class<?> callbackCls = classForName("org.eclipse.swt.internal.Callback");
		
		try {
			Constructor<?> consCallback = callbackCls.getConstructor(new Class<?>[] {
				Object.class,
				String.class,
				int.class
			});
			callBack3 = consCallback.newInstance(new Object[] {
				clazz,
				"actionProc",
				3
			});
			
			//callBack4 = new Callback(clazz, "actionProc", 4);
			callBack4 = consCallback.newInstance(new Object[] {
				clazz,
				"actionProc",
				4
			});
		} catch (Exception e) {
			
		}
	}*/
	
	private static Class<?> classForName(String classname) {
		try {
			Class<?> cls = Class.forName(classname);
			return cls;
		} catch (ClassNotFoundException e) {
			throw new IllegalStateException(e);
		}
	}
	
	//private Object delegate;
	
	private static Object wrapPointer(long value) {
		Class<?> PTR_CLASS = C.PTR_SIZEOF == 8 ? long.class : int.class;
		if (PTR_CLASS == long.class)
			return new Long(value);
		else
			return new Integer((int) value);
	}
	
	public CocoaUIEnhancer() throws Exception {
		
		byte[] SWT_OBJECT = {'S','W','T','_','O','B','J','E','C','T','\0'};
		
		Class<?> osCls = classForName("org.eclipse.swt.internal.cocoa.OS");
		Class<?> nsidCls = classForName("org.eclipse.swt.internal.cocoa.id");
		
		Log.d(TAG, "osCls = " + osCls);
		
		Object delegateObjSWTApplication = invoke(osCls, "objc_lookUpClass",
				new Object[] {
					"SWTApplicationDelegate"
				});
		long delegateIdSWTApplication = convertToLong(delegateObjSWTApplication);
		
		Class<?> swtapplicationdelegateCls = classForName("org.eclipse.swt.internal.cocoa.SWTApplicationDelegate");
		Object delegate = swtapplicationdelegateCls.newInstance();
		Object delegateAlloc = invoke(delegate, "alloc");
		invoke(delegateAlloc, "init");
		Object delegateIdObj = nsidCls.getField("id").get(delegate);
		long delegateJniRef = ((Number) invoke(osCls, "NewGlobalRef", new Class<?>[] {
			Object.class
		}, new Object[] {
			CocoaUIEnhancer.this
		})).longValue();
		if (delegateJniRef == 0)
			SWT.error(SWT.ERROR_NO_HANDLES);
		invoke(osCls, "object_setInstanceVariable", new Object[] {
			delegateIdObj,
			SWT_OBJECT,
			wrapPointer(delegateJniRef)
		});
	}
	
	private static Object invoke(Class<?> clazz, Object target,
			String methodName, Object[] args) {
		try {
			Class<?>[] signature = new Class<?>[args.length];
			for (int i = 0; i < args.length; i++) {
				Class<?> thisClass = args[i].getClass();
				if (thisClass == Integer.class)
					signature[i] = int.class;
				else if (thisClass == Long.class)
					signature[i] = long.class;
				else if (thisClass == Byte.class)
					signature[i] = byte.class;
				else if (thisClass == Boolean.class)
					signature[i] = boolean.class;
				else
					signature[i] = thisClass;
			}
			Method method = clazz.getMethod(methodName, signature);
			return method.invoke(target, args);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	private static Object invoke(Class<?> clazz, String methodName, Object[] args) {
		return invoke(clazz, null, methodName, args);
	}

	private static Object invoke(Object obj, String methodName) {
		return invoke(obj, methodName, (Class<?>[]) null, (Object[]) null);
	}

	private static Object invoke(Object obj, String methodName,
			Class<?>[] paramTypes, Object... arguments) {
		try {
			Method m = obj.getClass().getMethod(methodName, paramTypes);
			return m.invoke(obj, arguments);
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	private static long convertToLong(Object object) {
		if (object instanceof Integer) {
			Integer i = (Integer) object;
			return i.longValue();
		}
		if (object instanceof Long) {
			Long l = (Long) object;
			return l.longValue();
		}
		return 0;
	}
}
