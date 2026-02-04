#include <jni.h>
#include <vector>
#include <cassert>
#include <map>
#include "hr_unizg_pmf_matrixcalc_ui_service_MatrixServiceClientImpl.h"

struct sparseMatrix {
	std::vector<std::map<int,double>> values;
	int n,m;
	sparseMatrix(int n,int m) {
		this->n=n;
		this->m=m;
		values=std::vector<std::map<int,double>>(n);
	}
	sparseMatrix(JNIEnv *env,jobject x) {
		jclass matClass=env->GetObjectClass(x);
		jfieldID rowsFID=env->GetFieldID(matClass,"rows","I");
		jfieldID colsFID=env->GetFieldID(matClass,"cols","I");
		jfieldID entriesFID=env->GetFieldID(matClass,"entries","Ljava/util/List;");
		n=env->GetIntField(x,rowsFID);
		m=env->GetIntField(x,colsFID);
		values=std::vector<std::map<int,double>>(n);
		
		jobject entryList=env->GetObjectField(x,entriesFID);
		jclass listClass=env->GetObjectClass(entryList);
		jmethodID getIterator=env->GetMethodID(listClass,"iterator","()Ljava/util/Iterator;");
		jobject iterator=env->CallObjectMethod(entryList,getIterator);
		
		jclass iteratorClass=env->GetObjectClass(iterator);
		jmethodID hasNext=env->GetMethodID(iteratorClass,"hasNext","()Z");
		jmethodID next=env->GetMethodID(iteratorClass,"next","()Ljava/lang/Object;");
		
		while (env->CallBooleanMethod(iterator,hasNext)) {
		
		    jobject entry=env->CallObjectMethod(iterator,next);
		    jclass entryClass=env->GetObjectClass(entry);
		    jfieldID rowFID=env->GetFieldID(entryClass,"row","I");
			jfieldID colFID=env->GetFieldID(entryClass,"col","I");
			jfieldID valueFID=env->GetFieldID(entryClass,"value","D");
			int row=env->GetIntField(entry,rowFID);
			int col=env->GetIntField(entry,colFID);
			double val=env->GetDoubleField(entry,valueFID);
			values[row][col]=val;
		    
		    env->DeleteLocalRef(entry);
		}
	}
	void setValue(int i,int j,int x) {
		values[i][j]=x;
	}
	void addValue(int i,int j,int x) {
		values[i][j]+=x;
	}
	sparseMatrix mul(const sparseMatrix&rhs) {
		assert(m==rhs.n);
		sparseMatrix res(n,rhs.m);
		for (int i=0;i<n;++i) for (auto [j,val]: values[i]) {
			for (auto [k,rhsVal]: rhs.values[j]) {
				res.addValue(i,k,val*rhsVal);
			}
		}
		return res;
	}
	sparseMatrix solve(const sparseMatrix&rhs) {
		return rhs;
	}
	sparseMatrix Pinv() {
		return *this;
	}
	jobject toJObject(JNIEnv *env) {
		jclass matrixClass=(jclass)env->NewGlobalRef(env->FindClass("hr/unizg/pmf/matrixcalc/ui/dto/MatrixDTO"));
		jclass entryClass=(jclass)env->NewGlobalRef(env->FindClass("hr/unizg/pmf/matrixcalc/ui/dto/MatrixDTO$EntryDTO"));
		jclass arrayListClass=(jclass)env->NewGlobalRef(env->FindClass("java/util/ArrayList"));
		jmethodID matrixInit=env->GetMethodID(matrixClass,"<init>","(IILjava/util/List;)V");
		jmethodID entryInit=env->GetMethodID(entryClass,"<init>","(IID)V");
		jmethodID arrayListInit=env->GetMethodID(arrayListClass,"<init>","()V");
		jmethodID arrayListAdd=env->GetMethodID(arrayListClass,"add","(Ljava/lang/Object;)Z");
		jobject list=env->NewObject(arrayListClass,arrayListInit);
		for (int i=0;i<n;++i) for (auto [j,val]: values[i]) {
			jobject entry=env->NewObject(entryClass,entryInit,i,j,val);
			env->CallBooleanMethod(list,arrayListAdd,entry);
		}
		jobject res=env->NewObject(matrixClass,matrixInit,n,m,list);
		return res;
	}
};

JNIEXPORT jobject JNICALL Java_hr_unizg_pmf_matrixcalc_ui_service_MatrixServiceClientImpl_matMul(JNIEnv *env, jobject impl, jobject a, jobject b) {
	sparseMatrix matrixA(env,a),matrixB(env,b);
	sparseMatrix matrixRes=matrixA.mul(matrixB);
	jobject res=matrixRes.toJObject(env);
	return res;
}

JNIEXPORT jobject JNICALL Java_hr_unizg_pmf_matrixcalc_ui_service_MatrixServiceClientImpl_matSolve(JNIEnv *env, jobject impl, jobject a, jobject b) {
	sparseMatrix matrixA(env,a),matrixB(env,b);
	sparseMatrix matrixRes=matrixA.solve(matrixB);
	jobject res=matrixRes.toJObject(env);
	return res;
}

JNIEXPORT jobject JNICALL Java_hr_unizg_pmf_matrixcalc_ui_service_MatrixServiceClientImpl_matPinv(JNIEnv *env, jobject impl, jobject a) {
	sparseMatrix matrixA(env,a);
	sparseMatrix matrixRes=matrixA.Pinv();
	jobject res=matrixRes.toJObject(env);
	return res;
}
