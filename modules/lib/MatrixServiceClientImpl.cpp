#include <jni.h>
#include <vector>
#include <cassert>
#include <map>
#include "hr_unizg_pmf_matrixcalc_ui_service_MatrixServiceClientImpl.h"

const double eps=1e-12;

struct denseMatrix {
	std::vector<std::vector<double>> values;
	int n,m;
	denseMatrix(int n,int m) {
		this->n=n;
		this->m=m;
		values.resize(n);
		for (auto&x: values) x.resize(m);
	}
	denseMatrix transposed() const {
		denseMatrix res(m,n);
		for (int i=0;i<n;++i) for (int j=0;j<m;++j) res.values[j][i]=values[i][j];
		return res;
	}
	denseMatrix inverse() const {
		assert(n==m);
		denseMatrix res(n,n),cu=*this;
		for (int i=0;i<n;++i) res.values[i][i]=1;
		for (int i=0;i<n;++i) {
			double maxAbsVal=eps;
			int whe=-1;
			for (int r=i;r<n;++r) if (abs(cu.values[r][i])>maxAbsVal) {
				maxAbsVal=abs(cu.values[r][i]);
				whe=r;
			}
			assert(whe!=-1);
			swap(cu.values[whe],cu.values[i]);
			swap(res.values[whe],res.values[i]);
			double c=cu.values[i][i];
			for (int j=0;j<n;++j) cu.values[i][j]/=c,res.values[i][j]/=c;
			for (int j=i+1;j<n;++j) {
				double kol=cu.values[j][i];
				for (int k=0;k<n;++k) {
					cu.values[j][k]-=kol*cu.values[i][k];
					res.values[j][k]-=kol*res.values[i][k];
				}
			}
		}
		for (int i=n-1;i>0;--i) {
			for (int j=i-1;j>=0;--j) {
				double kol=cu.values[j][i];
				for (int k=0;k<n;++k) {
					cu.values[j][k]-=kol*cu.values[i][k];
					res.values[j][k]-=kol*res.values[i][k];
				}
			}
		}
		return res;
	}
	denseMatrix solve(denseMatrix b) const {
		std::vector<int> pivotCols;
		denseMatrix cu=*this;
		for (int i=0;i<m;++i) {
			double maxAbsVal=eps;
			int whe=-1,cr=pivotCols.size();
			for (int r=cr;r<n;++r) if (abs(cu.values[r][i])>maxAbsVal) {
				maxAbsVal=abs(cu.values[r][i]);
				whe=r;
			}
			if (whe==-1) continue;
			swap(cu.values[whe],cu.values[cr]);
			swap(b.values[whe],b.values[cr]);
			pivotCols.push_back(i);
			double c=cu.values[cr][i];
			for (int j=0;j<m;++j) cu.values[cr][j]/=c;
			b.values[cr][0]/=c;
			for (int j=cr+1;j<n;++j) {
				double kol=cu.values[j][i];
				for (int k=0;k<m;++k) {
					cu.values[j][k]-=kol*cu.values[cr][k];
				}
				b.values[j][0]-=kol*b.values[cr][0];
			}
		}
		denseMatrix res(m,1);
		for (int i=(int)pivotCols.size()-1;i>=0;--i) {
			res.values[pivotCols[i]][0]=b.values[i][0];
			for (int j=i-1;j>=0;--j) b.values[j][0]-=cu.values[j][pivotCols[i]]*res.values[pivotCols[i]][0];
		}
		return res;
	}
	std::pair<denseMatrix,denseMatrix> rankFactorization() const {
		std::vector<int> pivotCols;
		denseMatrix cu=*this;
		for (int i=0;i<m;++i) {
			double maxAbsVal=eps;
			int whe=-1,cr=pivotCols.size();
			for (int r=cr;r<n;++r) if (abs(cu.values[r][i])>maxAbsVal) {
				maxAbsVal=abs(cu.values[r][i]);
				whe=r;
			}
			if (whe==-1) continue;
			swap(cu.values[whe],cu.values[cr]);
			pivotCols.push_back(i);
			double c=cu.values[cr][i];
			for (int j=0;j<m;++j) cu.values[cr][j]/=c;
			for (int j=cr+1;j<n;++j) {
				double kol=cu.values[j][i];
				for (int k=0;k<m;++k) {
					cu.values[j][k]-=kol*cu.values[cr][k];
				}
			}
		}
		int rank=pivotCols.size();
		for (int i=rank-1;i>=0;--i) {
			for (int j=0;j<i;++j) {
				double kol=cu.values[j][pivotCols[i]];
				for (int k=0;k<n;++k) cu.values[j][k]-=kol*cu.values[i][k];
			}
		}
		denseMatrix c(n,rank),f(rank,m);
		for (int i=0;i<rank;++i) for (int j=0;j<m;++j) f.values[i][j]=cu.values[i][j];
		for (int i=0;i<n;++i) for (int j=0;j<rank;++j) c.values[i][j]=values[i][pivotCols[j]];
		return {c,f};
	}
	denseMatrix mul(const denseMatrix&rhs) const {
		assert(m==rhs.n);
		denseMatrix res(n,rhs.m);
		for (int i=0;i<n;++i) for (int j=0;j<m;++j) for (int k=0;k<rhs.m;++k) res.values[i][k]+=values[i][j]*rhs.values[j][k];
		return res;
	}
	denseMatrix Pinv() const {
		auto fac=rankFactorization();
		denseMatrix b=fac.first,c=fac.second;
		denseMatrix cpart=(c.mul(c.transposed())).inverse();
		denseMatrix bpart=(b.transposed().mul(b)).inverse();
		return c.transposed().mul(cpart).mul(bpart).mul(b.transposed());
	}
};

struct sparseMatrix {
	std::vector<std::map<int,double>> values;
	int n,m;
	sparseMatrix(int n,int m) {
		this->n=n;
		this->m=m;
		values=std::vector<std::map<int,double>>(n);
	}
	sparseMatrix(const denseMatrix&a) {
		this->n=a.n;
		this->m=a.m;
		values=std::vector<std::map<int,double>>(n);
		for (int i=0;i<n;++i) for (int j=0;j<m;++j) if (abs(a.values[i][j])>eps) values[i][j]=a.values[i][j];
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
	denseMatrix toDenseMatrix() const {
		denseMatrix res(n,m);
		for (int i=0;i<n;++i) for (auto [j,val]: values[i]) res.values[i][j]=val;
		return res;
	}
	sparseMatrix mul(const sparseMatrix&rhs) const {
		assert(m==rhs.n);
		sparseMatrix res(n,rhs.m);
		for (int i=0;i<n;++i) for (auto [j,val]: values[i]) {
			for (auto [k,rhsVal]: rhs.values[j]) {
				res.addValue(i,k,val*rhsVal);
			}
		}
		return res;
	}
	sparseMatrix solve(const sparseMatrix&rhs) const {
		return sparseMatrix(toDenseMatrix().solve(rhs.toDenseMatrix()));
	}
	sparseMatrix Pinv() const {
		return sparseMatrix(toDenseMatrix().Pinv());
	}
	jobject toJObject(JNIEnv *env) const {
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
